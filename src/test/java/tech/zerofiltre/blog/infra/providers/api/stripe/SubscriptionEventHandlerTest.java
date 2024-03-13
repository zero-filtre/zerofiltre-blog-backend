package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionRetrieveParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.Suspend;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.CANCELLED_3TIMES_PAID;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.USER_ID;

@ExtendWith(MockitoExtension.class)
class SubscriptionEventHandlerTest {

    @Mock
    Suspend suspend;

    @Mock
    Event event;

    @Mock
    CourseProvider courseProvider;

    @Mock
    PurchaseProvider purchaseProvider;

    @Mock
    ChapterProvider chapterProvider;

    @Mock
    EnrollmentProvider enrollmentProvider;

    @Mock
    Customer customer;

    @Mock
    Subscription subscription;

    @Mock
    UserProvider userProvider;

    SubscriptionEventHandler eventHandler;

    @BeforeEach
    void init() {
        eventHandler = new SubscriptionEventHandler(enrollmentProvider, courseProvider, chapterProvider, purchaseProvider, userProvider);

    }


    @Test
    void mustIgnore_DeletedSubscription_IfItIs_3TimesPaymentCancelled() throws ZerofiltreException, StripeException {

        //arrange
        ReflectionTestUtils.setField(eventHandler, "suspend", suspend);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(CANCELLED_3TIMES_PAID, Boolean.toString(true));
        when(subscription.getMetadata()).thenReturn(metadata);

        //act
        eventHandler.handleSubscriptionDeleted(event, subscription);

        //assert
        verify(suspend, times(0)).all(anyLong(), any());

    }

    @Test
    void mustSuspend_allEnrollments_onDeleted_ProPlanSubscription() throws ZerofiltreException, StripeException {

        try (MockedStatic<Subscription> mockedStatic = Mockito.mockStatic(Subscription.class)) {
            //arrange
            mockedStatic.when(() -> Subscription.retrieve(anyString(), any(SubscriptionRetrieveParams.class), eq(null)))
                    .thenReturn(subscription);

            when(subscription.getId()).thenReturn("subscriptionId");

            when(subscription.getCustomerObject()).thenReturn(customer);

            when(subscription.getMetadata()).thenReturn(Collections.emptyMap());

            Map<String, String> metadata = new HashMap<>();
            metadata.put(USER_ID, "15");
            when(customer.getMetadata()).thenReturn(metadata);

            when(event.getId()).thenReturn("eventId");
            when(event.getType()).thenReturn("eventType");

            ReflectionTestUtils.setField(eventHandler, "suspend", suspend);

            doNothing().when(suspend).all(anyLong(), eq(User.Plan.PRO));

            User user = mock(User.class);
            when(userProvider.userOfId(15L)).thenReturn(Optional.ofNullable(user));
            when(userProvider.save(any())).thenReturn(user);



            //act
            eventHandler.handleSubscriptionDeleted(event, subscription);

            //assert
            verify(suspend, times(1)).all(15, User.Plan.PRO);
            verify(user, times(1)).setPlan(User.Plan.BASIC);
            verify(userProvider, times(1)).save(any());
        }
    }
}