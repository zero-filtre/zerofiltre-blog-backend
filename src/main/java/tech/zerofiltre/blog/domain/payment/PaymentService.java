package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.payment.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class PaymentService {

    private final PaymentProvider paymentProvider;

    public PaymentService(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public ChargeResult charge(User user, ChargeRequest chargeRequest) throws PaymentException {
        //TODO product must be available according to its type, then use its price
        chargeRequest.setAmount(2000);

        //TODO build description from product
        chargeRequest.setDescription("Example charge for " + user.getEmail());

        chargeRequest.setEmail(user.getEmail());
        chargeRequest.setToken(chargeRequest.getToken());
        chargeRequest.setProductType(chargeRequest.getProductType());

        return paymentProvider.charge(chargeRequest);
    }

}
