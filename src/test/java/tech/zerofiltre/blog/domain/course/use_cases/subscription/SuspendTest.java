package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.doubles.*;

import java.time.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class SuspendTest {
    private Suspend suspend;

    @Test
    void suspendThrowsExceptionWhenUserIsNotSubscribedToCourse() {
        SubscriptionProvider subscriptionProvider = new NotSubscribedSubscriptionProvider();
        suspend = new Suspend(subscriptionProvider, new Found_Published_WithUnknownAuthor_CourseProviderSpy());
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> suspend.execute(1, 1))
                .withMessage("You are not subscribed to the course of id 1");
    }

    @Test
    void suspendDeactivatesTheSubscription() throws BlogException {
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        LocalDateTime beforeSuspend = LocalDateTime.now();
        suspend = new Suspend(subscriptionProvider, courseProvider);
        LocalDateTime afterSuspendPlus10Sec = LocalDateTime.now().plusSeconds(10);

        Subscription deactivatedSubscription = suspend.execute(1, 1);
        assertThat(courseProvider.enrollCalledCount).isTrue();

        Assertions.assertThat(deactivatedSubscription).isNotNull();
        Assertions.assertThat(deactivatedSubscription.isActive()).isFalse();
        Assertions.assertThat(subscriptionProvider.saveCalled).isTrue();

        org.assertj.core.api.Assertions.assertThat(deactivatedSubscription.getSuspendedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(deactivatedSubscription.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        org.assertj.core.api.Assertions.assertThat(deactivatedSubscription.getSuspendedAt()).isBeforeOrEqualTo(afterSuspendPlus10Sec);

    }
}
