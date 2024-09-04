package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;

public class PaymentException extends ZerofiltreException {

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
