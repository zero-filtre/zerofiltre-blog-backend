package tech.zerofiltre.blog.infra.entrypoints.rest.payment;

import com.stripe.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.payment.*;
import tech.zerofiltre.blog.domain.payment.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.payment.model.*;

import javax.validation.*;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final SecurityContextManager securityContextManager;
    private final CourseProvider courseProvider;
    private final PaymentService paymentService;


    public PaymentController(SecurityContextManager securityContextManager, CourseProvider courseProvider, InfraProperties infraProperties, PaymentProvider paymentProvider, UserProvider userProvider, EnrollmentProvider enrollmentProvider, ChapterProvider chapterProvider) {
        this.securityContextManager = securityContextManager;
        this.courseProvider = courseProvider;
        Suspend suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider);
        this.paymentService = new PaymentService(paymentProvider, userProvider, suspend);
        Stripe.apiKey = infraProperties.getStripeSecretKey();
    }

    @PostMapping("/cancelPlan")
    public String cancel() throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        paymentService.cancelSubscription(user);
        return "Plan cancelled";

    }

    @PostMapping("/checkout")
    public String createCheckoutSession(@RequestBody @Valid ChargeRequestVM chargeRequestVM) throws PaymentException, ResourceNotFoundException {
        User user = securityContextManager.getAuthenticatedUser();
        Product product = getProduct(chargeRequestVM.getProductId(), chargeRequestVM.getProductType());
        ChargeRequest chargeRequest = fromVM(chargeRequestVM);
        return paymentService.createCheckoutSession(user, product, chargeRequest);
    }

    private ChargeRequest fromVM(ChargeRequestVM chargeRequestVM) {
        ChargeRequest chargeRequest = new ChargeRequest();
        chargeRequest.setProductType(chargeRequestVM.getProductType());
        chargeRequest.setProductId(chargeRequest.getProductId());
        chargeRequest.setMode(chargeRequestVM.getMode());
        chargeRequest.setProPlan(chargeRequestVM.isProPlan());
        chargeRequest.setRecurringInterval(chargeRequestVM.getRecurringInterval());
        return chargeRequest;
    }


    @PostMapping("/webhook")
    public String handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws PaymentException {
        return paymentService.fulfill(payload, sigHeader);
    }


    private Product getProduct(long productId, ChargeRequest.ProductType productType) throws ResourceNotFoundException {
        if (!supportedProduct(productType))
            throw new IllegalArgumentException("Product type not supported");

        return courseProvider
                .courseOfId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product nof found", String.valueOf(productId), ""));
    }

    private boolean supportedProduct(ChargeRequest.ProductType productType){
       return  productType == ChargeRequest.ProductType.COURSE || productType == ChargeRequest.ProductType.MENTORED;
    }

}


