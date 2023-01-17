package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.doubles.*;

import java.time.*;

class SubscribeTest {

    private Subscribe subscribe;


    @Test
    void executeThrowsResourceNotFoundIfUserNotFound() {
        SubscriptionProvider subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new NotFoundUserProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> subscribe.execute(1, 1))
                .withMessage("We could not find the user with id 1");
    }

    @Test
    void executeThrowsResourceNotFoundIfCourseNotFound() {
        SubscriptionProvider subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new NotFoundCourseProviderSpy();
        UserProvider userProvider = new FoundNonAdminUserProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> subscribe.execute(1, 1))
                .withMessage("We couldn't find the course of id 1 you are trying to subscribe to");
    }

    @Test
    void executeThrowsForbiddenActionIfCourseIsNotPublished() {
        SubscriptionProvider subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundNonAdminUserProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider);

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> subscribe.execute(1, 1))
                .withMessage("You can not subscribe to an unpublished course");
    }

    @Test
    void executeSavesSubscriptionProperly() throws ResourceNotFoundException, ForbiddenActionException {
        SubscriptionProvider subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundNonAdminUserProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider);

        LocalDateTime beforeSubscribe = LocalDateTime.now();
        Subscription subscription = subscribe.execute(1, 1);
        LocalDateTime afterSubscribe = LocalDateTime.now();

        Assertions.assertThat(subscription).isNotNull();
        Assertions.assertThat(subscription.getId()).isNotZero();
        Assertions.assertThat(subscription.getCourse().getId()).isNotZero();
        Assertions.assertThat(subscription.getSubscriber().getId()).isNotZero();
        Assertions.assertThat(subscription.isCompleted()).isFalse();
        Assertions.assertThat(subscription.getCompletedLessons()).isEmpty();
        Assertions.assertThat(subscription.getSubscribedAt()).isNotNull();
        Assertions.assertThat(subscription.getSubscribedAt()).isAfterOrEqualTo(beforeSubscribe);
        Assertions.assertThat(subscription.getSubscribedAt()).isBeforeOrEqualTo(afterSubscribe);
        Assertions.assertThat(subscription.isActive()).isTrue();
    }
}