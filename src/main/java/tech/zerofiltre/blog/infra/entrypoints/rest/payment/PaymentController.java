package tech.zerofiltre.blog.infra.entrypoints.rest.payment;

import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.Suspend;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.PaymentProvider;
import tech.zerofiltre.blog.domain.payment.PaymentService;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.payment.model.ChargeRequestVM;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final SecurityContextManager securityContextManager;
    private final CourseProvider courseProvider;
    private final PaymentService stripePaymentService;
    private final PaymentService notchPaymentService;


    public PaymentController(SecurityContextManager securityContextManager,
                             CourseProvider courseProvider,
                             InfraProperties infraProperties,
                             @Qualifier("stripeProvider") PaymentProvider stripeProvider,
                             @Qualifier("notchPayProvider") PaymentProvider notchPayProvider,
                             UserProvider userProvider,
                             EnrollmentProvider enrollmentProvider, ChapterProvider chapterProvider, PurchaseProvider purchaseProvider) {
        this.securityContextManager = securityContextManager;
        this.courseProvider = courseProvider;
        Suspend suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider, purchaseProvider);
        stripePaymentService = new PaymentService(stripeProvider, userProvider, suspend);
        notchPaymentService = new PaymentService(notchPayProvider, userProvider, suspend);
        Stripe.apiKey = infraProperties.getStripeSecretKey();
    }

    @PostMapping("/cancelPlan")
    public String cancel() throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        stripePaymentService.cancelSubscription(user);
        return "Plan cancelled";

    }

    @PostMapping("/checkout")
    public String createCheckoutSession(@RequestBody @Valid ChargeRequestVM chargeRequestVM) throws PaymentException, ResourceNotFoundException {
        User user = securityContextManager.getAuthenticatedUser();
        Product product = getProduct(chargeRequestVM.getProductId(), chargeRequestVM.getProductType());
        ChargeRequest chargeRequest = fromVM(chargeRequestVM);
        if (ChargeRequest.Currency.XAF.equals(chargeRequest.getCurrency())) {
            return notchPaymentService.createCheckoutSession(user, product, chargeRequest);
        }
        return stripePaymentService.createCheckoutSession(user, product, chargeRequest);
    }

    private ChargeRequest fromVM(ChargeRequestVM chargeRequestVM) {
        ChargeRequest chargeRequest = new ChargeRequest();
        chargeRequest.setProductType(chargeRequestVM.getProductType());
        chargeRequest.setProductId(chargeRequest.getProductId());
        chargeRequest.setMode(chargeRequestVM.getMode());
        chargeRequest.setProPlan(chargeRequestVM.isProPlan());
        chargeRequest.setRecurringInterval(chargeRequestVM.getRecurringInterval());
        chargeRequest.setCurrency(chargeRequestVM.getCurrency());
        chargeRequest.setPaymentEmail(chargeRequestVM.getPaymentEmail());
        return chargeRequest;
    }


    @PostMapping("/webhook")
    public String handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader, @RequestHeader(value = "x-notch-signature", required = false) String notchPaySigHeader) throws PaymentException {
        if (sigHeader == null || sigHeader.isBlank()) {
            return notchPaymentService.fulfill(payload, notchPaySigHeader);
        } else {
            return stripePaymentService.fulfill(payload, sigHeader);
        }
    }


    private Product getProduct(long productId, ChargeRequest.ProductType productType) throws ResourceNotFoundException {
        if (!supportedProduct(productType))
            throw new IllegalArgumentException("Product type not supported");

        return courseProvider
                .courseOfId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product nof found", String.valueOf(productId), ""));
    }

    private boolean supportedProduct(ChargeRequest.ProductType productType) {
        return productType == ChargeRequest.ProductType.COURSE || productType == ChargeRequest.ProductType.MENTORED;
    }

}


