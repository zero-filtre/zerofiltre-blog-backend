package tech.zerofiltre.blog.infra.providers.api.notchpay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.PaymentProvider;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.user.model.User;

@Slf4j
@Component
public class NotchPayProvider implements PaymentProvider {
    @Override
    public String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException {
        return "";
    }

    @Override
    public String handleWebhook(String payload, String signature) throws PaymentException {
        return "";
    }

    @Override
    public void cancelSubscription(String paymentCustomerId) throws PaymentException {

    }
}
