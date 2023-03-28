package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.payment.model.*;

public interface PaymentProvider {
    ChargeResult charge(ChargeRequest chargeRequest) throws PaymentException;
}
