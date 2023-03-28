package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.payment.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

public interface PaymentProvider {

    String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException;

    String handleWebhook(String payload, String signature) throws PaymentException;
}
