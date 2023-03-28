package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.model.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.use_cases.subscription.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.providers.notification.user.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

@Slf4j
@Component
public class StripeCommons {

    public static final String USER_ID = "userId";
    public static final String PRO_PLAN_PRICE_ID = "price_1MjU8aFbuS9bqsyPr9G6P45y";
    public static final String PRO_PLAN_YEARLY_PRICE_ID = "price_1MnyeSFbuS9bqsyPZVGSeHgA";
    public static final String PRODUCT_ID = "productId";
    public static final String INVALID_PAYLOAD = "Invalid payload";
    public static final String PRO_PLAN_PRODUCT_ID = "prod_NUT4DYfDGPiLbR";
    public static final String VOTRE_PAIEMENT_CHEZ_ZEROFILTRE = "Votre paiement chez Zerofiltre";
    public static final String SIGNATURE = "\n\n L'équipe Zerofiltre";
    public static final String SUBSCRIPTION_CREATE_BILLING_REASON = "subscription_create";
    public static final String TOTAL_PAID_COUNT = "totalPaidCount";
    public static final String EVENT_ID_EVENT_TYPE_PRICE = "EventId= {}, EventType={}, Price: {}";

    private final UserProvider userProvider;
    private final Subscribe subscribe;
    private final Suspend suspend;
    private final BlogEmailSender emailSender;

    public StripeCommons(UserProvider userProvider, SubscriptionProvider subscriptionProvider, CourseProvider courseProvider, ChapterProvider chapterProvider, BlogEmailSender emailSender) {
        this.userProvider = userProvider;
        this.emailSender = emailSender;
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);
        suspend = new Suspend(subscriptionProvider, courseProvider, chapterProvider);
    }

    public void fulfillOrder(String userId, com.stripe.model.Product productObject, boolean paymentSuccess, Event event, Customer customer) throws BlogException {
        if (productObject == null) return;
        log.info("EventId= {}, EventType={}, Product object: {}", event.getId(), event.getType(), productObject.toString().replace("\n", " "));

        log.info("EventId= {}, EventType={},User id: {}", event.getId(), event.getType(), userId);

        if (PRO_PLAN_PRODUCT_ID.equals(productObject.getId())) { //subscription to PRO
            log.info("EventId= {}, EventType={}, User {} is subscribing to pro plan", event.getId(), event.getType(), userId);
            updateUserInfo(userId, paymentSuccess, event, customer, true);
            log.info("EventId= {}, EventType={}, User {} has subscribed to pro plan", event.getId(), event.getType(), userId);
        } else {
            long productId = Long.parseLong(productObject.getMetadata().get(PRODUCT_ID));
            log.info("EventId= {}, EventType={}, Product id: {}", event.getId(), event.getType(), productId);
            if (paymentSuccess) {
                subscribe.execute(Long.parseLong(userId), productId);
                log.info("EventId= {}, EventType={}, User of id={} enrolled in Product id: {}", event.getId(), event.getType(), userId, productId);
            } else {
                suspend.execute(Long.parseLong(userId), productId);
                log.info("EventId= {}, EventType={}, User of id={} suspended from Product id: {}", event.getId(), event.getType(), userId, productId);
            }
            updateUserInfo(userId, paymentSuccess, event, customer, false);
        }
    }

    private void updateUserInfo(String userId, boolean paymentSuccess, Event event, Customer customer, boolean isPro) throws UserNotFoundException {
        User user = userProvider.userOfId(Long.parseLong(userId))
                .orElseThrow(() -> {
                    log.error("EventId= {}, EventType={}, We couldn't find the user {} to edit", event.getId(), event.getType(), userId);
                    return new UserNotFoundException("EventId= " + event.getId() + ",EventType= " + event.getType() + " We couldn't find the user " + userId + " to edit", userId);
                });
        if (isPro) user.setPlan(paymentSuccess ? User.Plan.PRO : User.Plan.BASIC);
        String paymentEmail = customer.getEmail();
        user.setPaymentEmail(paymentEmail);
        userProvider.save(user);
    }

    public void notifyUser(Customer customer, String subject, String message) {
        try {
            Email email = new Email();
            email.setRecipients(Collections.singletonList(customer.getEmail()));
            email.setSubject(subject);
            email.setReplyTo("info@zerofiltre.tech");
            email.setContent(message);
            emailSender.send(email);
        } catch (Exception e) {
            log.warn("Failed to notify user {} about payment with this subject {} with message {}", customer != null ? customer.getEmail() : "unknown user", subject, message);
        }
    }


}
