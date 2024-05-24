package tech.zerofiltre.blog.infra.providers.api.notchpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
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
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.PaymentProvider;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.api.notchpay.model.NotchPaymentPaylod;
import tech.zerofiltre.blog.infra.providers.api.notchpay.model.WebhookPayload;
import tech.zerofiltre.blog.infra.providers.database.payment.DBNotchPayProvider;
import tech.zerofiltre.blog.infra.providers.notification.user.ZerofiltreEmailSender;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Qualifier("notchPayProvider")
public class NotchPayProvider implements PaymentProvider {
    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;
    private final UserProvider userProvider;
    private final DBNotchPayProvider dbNotchPayProvider;
    private final ZerofiltreEmailSender emailSender;

    public NotchPayProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate, UserProvider userProvider, DBNotchPayProvider dbNotchPayProvider, ZerofiltreEmailSender emailSender) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        this.retryTemplate = retryTemplate;
        this.userProvider = userProvider;
        this.dbNotchPayProvider = dbNotchPayProvider;
        this.emailSender = emailSender;
    }

    @Override
    public String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException {

        NotchPaymentPaylod body = new NotchPaymentPaylod();
        body.setEmail(chargeRequest.getPaymentEmail());
        body.setAmount(6550);
        body.setCurrency(chargeRequest.getCurrency().getValue());
        body.setDescription("Abonnement PRO à la plateforme Zerofiltre");
        String paymentReference = UUID.randomUUID().toString();
        body.setReference(paymentReference);
        body.getCustomerMeta().put("userId", user.getId());

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Authorization", infraProperties.getNotchPayPublicKey());

        try {
            return retryTemplate.execute(retryContext -> {
                HttpEntity<NotchPaymentPaylod> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.exchange(infraProperties.getNotchPayUrl() + "/payments/initialize", HttpMethod.POST, requestEntity, String.class);
                String responseBody = response.getBody();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode;
                rootNode = mapper.readTree(responseBody);
                JsonNode code = rootNode.get("code");
                JsonNode authorizationUrl = rootNode.get("authorization_url");
                if (code != null && code.asText().equals("201")) {
                    Payment payment = new Payment();
                    payment.setReference(paymentReference);
                    payment.setUser(user);
                    save(payment);
                    user.setPaymentEmail(chargeRequest.getPaymentEmail());
                    userProvider.save(user);
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
            String computedHash = new HmacUtils("HmacSHA256", infraProperties.getNotchPayHash()).hmacHex(payload);
            if (!computedHash.equals(signature)) throw new PaymentException("Invalid notchpay signature", "payment");
            WebhookPayload webhookPayload = new ObjectMapper().readValue(payload, WebhookPayload.class);
            if (!"payment.complete".equals(webhookPayload.getEvent()))
                return "Nothing was done as payment was not completed";
            String paymentReference = webhookPayload.getData().getMerchantReference();
            Optional<Payment> payment = paymentOf(paymentReference);
            if (payment.isEmpty()) throw new PaymentException("Initialized payment not found in db", "payment");
            User user = userProvider.userOfId(payment.get().getUser().getId()).orElseThrow(() -> new PaymentException("Unable to find the user of the payment", "payment"));
            user.setPlan(User.Plan.PRO);
            userProvider.save(user);
//            scheduleReminderToRenew();
            String originURL = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());

            notifyUser(user,
                    "Votre paiement chez Zerofiltre",
                    "Merci de faire confiance à Zerofiltre.tech, vous pouvez dès à présent bénéficier de nos contenus dans leur entièreté: " +
                            originURL +
                            "\n\n Vous recevrez un rappel 5 jours avant la date de renouvellement de votre abonnement afin d'éviter toute coupure." +
                            "\n\n L'équipe Zerofiltre. ");
            return "OK";
        } catch (Exception e) {
            throw new PaymentException("Unable to process notchpay webhook", e, "payment");
        }
    }

    @Override
    public void cancelSubscription(String paymentCustomerId) throws PaymentException {
//            cancelScheduleReminderToRenew();
        //            notifyUser();
    }

    @Override
    public Optional<tech.zerofiltre.blog.domain.payment.model.Payment> paymentOf(String reference) {
        return dbNotchPayProvider.paymentOf(reference);
    }

    @Override
    public tech.zerofiltre.blog.domain.payment.model.Payment save(tech.zerofiltre.blog.domain.payment.model.Payment payment) throws ZerofiltreException {
        return dbNotchPayProvider.save(payment);
    }

    void notifyUser(User user, String subject, String message) {
        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        String emailAddress = validEmail ? user.getEmail() : user.getPaymentEmail();
        if (emailAddress != null) {
            Email email = new Email();
            email.setRecipients(Collections.singletonList(emailAddress));
            email.setSubject(subject);
            email.setReplyTo("info@zerofiltre.tech");
            email.setContent(message);
            emailSender.send(email, false);
        }
    }

}
