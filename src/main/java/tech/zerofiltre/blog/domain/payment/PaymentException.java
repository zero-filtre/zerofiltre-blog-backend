package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.error.*;

public class PaymentException extends ZerofiltreException {

    public PaymentException(String message, String domain) {
        super(message, domain);
    }

    public PaymentException(String message, Throwable cause, String domain) {
        super(message, cause, domain);
    }
}
