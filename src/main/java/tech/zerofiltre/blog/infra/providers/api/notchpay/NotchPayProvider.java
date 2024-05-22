package tech.zerofiltre.blog.infra.providers.api.notchpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.PaymentProvider;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.api.notchpay.model.Payment;
import tech.zerofiltre.blog.infra.providers.api.notchpay.model.WebhookPayload;

import java.util.UUID;

@Slf4j
@Component
@Qualifier("notchPayProvider")
public class NotchPayProvider implements PaymentProvider {
    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;
    private final UserProvider userProvider;

    public NotchPayProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate, UserProvider userProvider) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        this.retryTemplate = retryTemplate;
        this.userProvider = userProvider;
    }

    @Override
    public String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException {

        Payment body = new Payment();
        body.setEmail(chargeRequest.getPaymentEmail());
        body.setAmount(6550);
        body.setCurrency(chargeRequest.getCurrency().getValue());
        body.setDescription("Abonnement PRO à la plateforme Zerofiltre");
        body.setReference(UUID.randomUUID().toString());
        body.getCustomerMeta().put("userId", user.getId());

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Authorization", infraProperties.getNotchPayPublicKey());

        try {
            return retryTemplate.execute(retryContext -> {
                HttpEntity<Payment> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.exchange(infraProperties.getNotchPayUrl() + "/payments/initialize", HttpMethod.POST, requestEntity, String.class);
                String responseBody = response.getBody();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = null;
                rootNode = mapper.readTree(responseBody);
                JsonNode code = rootNode.get("code");
                JsonNode authorizationUrl = rootNode.get("authorization_url");
                if (code != null && code.asText().equals("201")) {
                    return authorizationUrl.asText();
                } else {
                    throw new PaymentException("We couldn't initialize the payment due to : " + responseBody, "payment");
                }
            });
        } catch (Exception e) {
            log.error("We couldn't initialize the payment", e);
            throw new PaymentException("We couldn't initialize the payment", e, "payment");
        }
    }

    @Override
    public String handleWebhook(String payload, String signature) throws PaymentException {
        try {
            String computedHash = DigestUtils.sha256Hex(infraProperties.getNotchPayHash());
            if (!computedHash.equals(signature)) throw new PaymentException("Invalid notchpay signature", "payment");
            WebhookPayload webhookPayload = new ObjectMapper().readValue(payload, WebhookPayload.class);
            if (!"payment.complete".equals(webhookPayload.getEvent()))
                return "Nothing was done as payment was not completed";
            long userId = (long) webhookPayload.getData().getMetadata().get("userId");
            User user = userProvider.userOfId(userId).orElseThrow(() -> new PaymentException("Unable to find the user of the payment", "payment"));
            user.setPlan(User.Plan.PRO);
            userProvider.save(user);
//            scheduleReminderToRenew();
//            notifyUser();
            return "OK";
        } catch (Exception e) {
            throw new PaymentException("Unable to process notchpay webhook", e, "payment");
        }
    }

    @Override
    public void cancelSubscription(String paymentCustomerId) throws PaymentException {

    }
}
