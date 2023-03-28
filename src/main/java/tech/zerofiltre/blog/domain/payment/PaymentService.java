package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.payment.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class PaymentService {

    private final PaymentProvider paymentProvider;


    public PaymentService(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String createCheckoutSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException {
        return paymentProvider.createSession(user,product,chargeRequest);
    }

    public String fulfill(String payload, String signature) throws PaymentException {
        return paymentProvider.handleWebhook(payload,signature);
    }
}
