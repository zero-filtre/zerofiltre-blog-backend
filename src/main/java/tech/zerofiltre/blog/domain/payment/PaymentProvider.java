package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.user.model.User;

public interface PaymentProvider {

    String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException;

    String handleWebhook(String payload, String signature) throws PaymentException;

    void cancelSubscription(String paymentCustomerId) throws PaymentException;

}
