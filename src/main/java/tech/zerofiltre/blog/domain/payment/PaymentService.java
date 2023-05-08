package tech.zerofiltre.blog.domain.payment;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.payment.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class PaymentService {

    private final PaymentProvider paymentProvider;
    private final UserProvider userProvider;
    private final Suspend suspend;

    public PaymentService(PaymentProvider paymentProvider, UserProvider userProvider, Suspend suspend) {
        this.paymentProvider = paymentProvider;
        this.userProvider = userProvider;
        this.suspend = suspend;
    }

    public String createCheckoutSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException {
        return paymentProvider.createSession(user, product, chargeRequest);
    }

    public String fulfill(String payload, String signature) throws PaymentException {
        return paymentProvider.handleWebhook(payload, signature);
    }

    public void cancelSubscription(User user) throws ZerofiltreException {
        paymentProvider.cancelSubscription(user.getPaymentCustomerId());
        user.setPlan(User.Plan.BASIC);
        userProvider.save(user);
        suspend.all(user.getId(), User.Plan.PRO);
    }
}
