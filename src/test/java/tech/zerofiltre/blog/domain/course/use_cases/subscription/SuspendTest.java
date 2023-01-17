package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.doubles.*;

import java.time.*;

class SuspendTest {
    private Suspend suspend;

    @Test
    void suspendThrowsExceptionWhenUserIsNotSubscribedToCourse() {
        SubscriptionProvider subscriptionProvider = new NotSubscribedSubscriptionProvider();

        suspend = new Suspend(subscriptionProvider);

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> suspend.execute(1, 1))
                .withMessage("You are not subscribed to the course of id 1");
    }

    @Test
    void suspendDeactivatesTheSubscription() throws ForbiddenActionException {
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        LocalDateTime beforeSuspend = LocalDateTime.now();
        suspend = new Suspend(subscriptionProvider);
        LocalDateTime afterSuspendPlus10Sec = LocalDateTime.now().plusSeconds(10);

        Subscription deactivatedSubscription = suspend.execute(1, 1);
        Assertions.assertThat(deactivatedSubscription).isNotNull();
        Assertions.assertThat(deactivatedSubscription.isActive()).isFalse();
        Assertions.assertThat(subscriptionProvider.saveCalled).isTrue();

        org.assertj.core.api.Assertions.assertThat(deactivatedSubscription.getSuspendedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(deactivatedSubscription.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        org.assertj.core.api.Assertions.assertThat(deactivatedSubscription.getSuspendedAt()).isBeforeOrEqualTo(afterSuspendPlus10Sec);

    }
}
