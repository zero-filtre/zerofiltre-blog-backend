package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionRetrieveParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.Suspend;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Map;
import java.util.Optional;

import static tech.zerofiltre.blog.domain.user.model.User.Plan.BASIC;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.CANCELLED_3TIMES_PAID;

@Slf4j
@Component
public class SubscriptionEventHandler {
    private static final String USER_ID = "userId";


    private final Suspend suspend;
    private final UserProvider userProvider;


    public SubscriptionEventHandler(EnrollmentProvider enrollmentProvider, CourseProvider courseProvider, ChapterProvider chapterProvider, PurchaseProvider purchaseProvider, UserProvider userProvider) {
        this.userProvider = userProvider;
        this.suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider, purchaseProvider);
    }

    public void handleSubscriptionDeleted(Event event, Subscription subscription) throws ZerofiltreException, StripeException {
        Map<String, String> metadata = subscription.getMetadata();
        if (metadata.containsKey(CANCELLED_3TIMES_PAID) && Boolean.toString(true).equals(metadata.get(CANCELLED_3TIMES_PAID)))
            return;
        SubscriptionRetrieveParams subscriptionRetrieveParams = SubscriptionRetrieveParams.builder().addExpand("customer").build();
        subscription = Subscription.retrieve(subscription.getId(), subscriptionRetrieveParams, null);
        Customer customer = subscription.getCustomerObject();
        log.debug("EventId= {}, EventType={}, Customer: {}", event.getId(), event.getType(), customer != null ? customer.toString().replace("\n", " ") : "no customer provided");
        String userId = customer != null && customer.getMetadata() != null ? customer.getMetadata().get(USER_ID) : "";
        log.debug("EventId= {}, EventType={}, User id: {}", event.getId(), event.getType(), userId);
        suspend.all(Long.parseLong(userId), User.Plan.PRO);
        Optional<User> foundUser = userProvider.userOfId(Long.parseLong(userId));
        foundUser.ifPresent(user -> {
            user.setPlan(BASIC);
            userProvider.save(user);
        });
    }
}
