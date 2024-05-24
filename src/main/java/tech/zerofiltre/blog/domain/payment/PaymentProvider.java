package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

public interface PaymentProvider {

    String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException;

    String handleWebhook(String payload, String signature) throws PaymentException;

    void cancelSubscription(String paymentCustomerId) throws PaymentException;

    Optional<Payment> paymentOf(String reference);

    Payment save(Payment payment) throws ZerofiltreException;
}
