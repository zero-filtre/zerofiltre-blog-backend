package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.model.Customer;
import com.stripe.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.Enroll;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.Suspend;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.use_cases.UserNotFoundException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.ZerofiltreEmailSender;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import java.util.*;

import static tech.zerofiltre.blog.domain.user.model.User.Plan.BASIC;
import static tech.zerofiltre.blog.domain.user.model.User.Plan.PRO;

@Slf4j
@Component
public class StripeCommons {

    public static final String USER_ID = "userId";
    public static final String PRODUCT_ID = "productId";
    public static final String VOTRE_PAIEMENT_CHEZ_ZEROFILTRE = "Votre paiement chez Zerofiltre";
    public static final String SUBSCRIPTION_CREATE_BILLING_REASON = "subscription_create";
    public static final String TOTAL_PAID_COUNT = "totalPaidCount";
    public static final String CANCELLED_3TIMES_PAID = "cancelled3TimesPaid";
    public static final String EVENT_ID_EVENT_TYPE_PRICE = "EventId= {}, EventType={}, Price: {}";
    public static final String EVENT_ID = "EventId= ";
    public static final String EVENT_TYPE = ",EventType= ";
    public static final String TO_EDIT = " to edit";

    private final UserProvider userProvider;
    private final CourseProvider courseProvider;
    private final Enroll enroll;
    private final Suspend suspend;
    private final ZerofiltreEmailSender emailSender;
    private final InfraProperties infraProperties;
    private final ITemplateEngine emailTemplateEngine;
    private final PurchaseProvider purchaseProvider;


    public StripeCommons(
            UserProvider userProvider,
            EnrollmentProvider enrollmentProvider,
            CourseProvider courseProvider,
            ChapterProvider chapterProvider,
            ZerofiltreEmailSender emailSender,
            InfraProperties infraProperties,
            ITemplateEngine emailTemplateEngine,
            PurchaseProvider purchaseProvider,
            SandboxProvider sandboxProvider) {

        this.userProvider = userProvider;
        this.emailSender = emailSender;
        this.infraProperties = infraProperties;
        this.emailTemplateEngine = emailTemplateEngine;
        this.courseProvider = courseProvider;
        this.purchaseProvider = purchaseProvider;
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider);
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider);
    }

    public void fulfillOrder(String userId, com.stripe.model.Product productObject, boolean paymentSuccess, Event event, Customer customer) throws ZerofiltreException {
        if (productObject == null) return;
        log.info("EventId= {}, EventType={}, Product object: {}", event.getId(), event.getType(), productObject.toString().replace("\n", " "));

        log.info("EventId= {}, EventType={},User id: {}", event.getId(), event.getType(), userId);

        if (infraProperties.getProPlanProductId().equals(productObject.getId())) { //subscription to PRO
            log.info("EventId= {}, EventType={}, Handling User {} pro plan subscription", event.getId(), event.getType(), userId);
            updateUserInfo(userId, paymentSuccess, event, customer, true);
            log.info("EventId= {}, EventType={}, Handled User {} pro plan subscription", event.getId(), event.getType(), userId);
        } else {
            long productId = Long.parseLong(productObject.getMetadata().get(PRODUCT_ID));
            log.info("EventId= {}, EventType={}, Product id: {}", event.getId(), event.getType(), productId);
            if (paymentSuccess) {
                purchase(userId, event, productId);
                log.info("EventId= {}, EventType={}, User of id={} enrolled in Product id: {}", event.getId(), event.getType(), userId, productId);
            } else {
                suspend.execute(Long.parseLong(userId), productId);
                log.info("EventId= {}, EventType={}, User of id={} suspended from Product id: {}", event.getId(), event.getType(), userId, productId);
            }
            updateUserInfo(userId, paymentSuccess, event, customer, false);
        }
    }

    private void purchase(String userId, Event event, long productId) throws ZerofiltreException {
        Optional<Purchase> foundPurchase = purchaseProvider.purchaseOf(Long.parseLong(userId), productId);
        if (foundPurchase.isEmpty()) {
            User user = userProvider.userOfId(Long.parseLong(userId))
                    .orElseThrow(() -> new UserNotFoundException(EVENT_ID + event.getId() + EVENT_TYPE + event.getType() + " We couldn't find the user " + userId + TO_EDIT, userId));
            Course course = courseProvider.courseOfId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(EVENT_ID + event.getId() + EVENT_TYPE + event.getType() + " We couldn't find the course " + productId + TO_EDIT, String.valueOf(productId), null));
            Purchase purchase = new Purchase(user, course);
            purchase = purchaseProvider.save(purchase);
            if (purchase.getId() != 0) enroll.execute(Long.parseLong(userId), productId);
        }
    }

    void notifyUser(Customer customer, String subject, String message) {
        try {
            Email email = new Email();
            email.setRecipients(Collections.singletonList(customer.getEmail()));
            email.setSubject(subject);
            email.setReplyTo("info@zerofiltre.tech");

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("content", message);
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            thymeleafContext.setLocale(Locale.FRENCH);

            String emailContent = emailTemplateEngine.process("general_message.html", thymeleafContext);
            email.setContent(emailContent);

            emailSender.send(email);
        } catch (Exception e) {
            log.warn("Failed to notify user {} about payment with this subject {} with message {}", customer != null ? customer.getEmail() : "unknown user", subject, message);
        }
    }

    void updateUserInfo(String userId, boolean paymentSuccess, Event event, Customer customer, boolean isPro) throws ZerofiltreException {
        User user = userProvider.userOfId(Long.parseLong(userId))
                .orElseThrow(() -> {
                    log.error("EventId= {}, EventType={}, We couldn't find the user {} to edit", event.getId(), event.getType(), userId);
                    return new UserNotFoundException(EVENT_ID + event.getId() + EVENT_TYPE + event.getType() + " We couldn't find the user " + userId + TO_EDIT, userId);
                });
        if (isPro && !paymentSuccess) {
            user.setPlan(BASIC);
            suspend.all(Long.parseLong(userId), false);
        } else if (isPro) {
            user.setPlan(PRO);
        }
        String paymentEmail = customer.getEmail();
        user.setPaymentEmail(paymentEmail);
        userProvider.save(user);
    }

}
