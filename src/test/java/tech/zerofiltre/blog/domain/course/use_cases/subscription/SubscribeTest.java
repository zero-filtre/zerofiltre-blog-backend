package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SubscribeTest {

    private Subscribe subscribe;

    @Test
    void executeThrowsResourceNotFoundIfUserNotFound() {
        FoundCancelledOnlySubscriptionProviderSpy subscriptionProvider = new FoundCancelledOnlySubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new NotFoundUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> subscribe.execute(1, 1))
                .withMessage("We could not find the user with id 1");
    }

    @Test
    void executeThrowsForbiddenActionIfUserNotAdminNorPRO() throws BlogException {
        SubscriptionProvider subscriptionProvider = mock(SubscriptionProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(subscriptionProvider.subscriptionOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        Subscription subscription = new Subscription();
        when(subscriptionProvider.save(any())).thenReturn(subscription);

        User user = ZerofiltreUtils.createMockUser(false);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        subscription.setCourse(course);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> subscribe.execute(user.getId(), course.getId()));

    }


    @Test
    void executeThrowsResourceNotFoundIfCourseNotFound() {
        FoundCancelledOnlySubscriptionProviderSpy subscriptionProvider = new FoundCancelledOnlySubscriptionProviderSpy();
        CourseProvider courseProvider = new NotFoundCourseProviderSpy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> subscribe.execute(1, 1))
                .withMessage("We couldn't find the course of id 1 you are trying to subscribe to");
    }

    @Test
    void executeThrowsForbiddenActionIfCourseIsNotPublished() {
        FoundCancelledOnlySubscriptionProviderSpy subscriptionProvider = new FoundCancelledOnlySubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> subscribe.execute(1, 1))
                .withMessage("You can not subscribe to an unpublished course");
    }

    @Test
    void executeSavesSubscriptionProperly() throws BlogException {
        NotFoundSubscriptionProviderDummy subscriptionProvider = new NotFoundSubscriptionProviderDummy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);

        LocalDateTime beforeSubscribe = LocalDateTime.now();
        Subscription subscription = subscribe.execute(1, 1);
        LocalDateTime afterSubscribe = LocalDateTime.now();

        Course course = subscription.getCourse();
        assertThat(course.getEnrolledCount()).isEqualTo(1);
        assertThat(courseProvider.enrollCalledCount).isTrue();

        assertThat(course.getLessonsCount()).isEqualTo(2);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();


        assertThat(courseProvider.enrollCalledCount).isTrue();
        Assertions.assertThat(subscription).isNotNull();
        Assertions.assertThat(subscription.getId()).isNotZero();
        Assertions.assertThat(subscription.getCourse().getId()).isNotZero();
        Assertions.assertThat(subscription.isCompleted()).isFalse();
        Assertions.assertThat(subscription.getCompletedLessons()).isEmpty();
        Assertions.assertThat(subscription.getSubscribedAt()).isNotNull();
        Assertions.assertThat(subscription.getSubscribedAt()).isAfterOrEqualTo(beforeSubscribe);
        Assertions.assertThat(subscription.getSubscribedAt()).isBeforeOrEqualTo(afterSubscribe);
        Assertions.assertThat(subscription.getLastModifiedAt()).isEqualTo(subscription.getSubscribedAt());
        Assertions.assertThat(subscription.getLastModifiedAt()).isAfterOrEqualTo(beforeSubscribe);
        Assertions.assertThat(subscription.getLastModifiedAt()).isBeforeOrEqualTo(afterSubscribe);
        Assertions.assertThat(subscription.isActive()).isTrue();
    }

    @Test
    @DisplayName("Subscribing after a suspension should set fields properly")
    void executeSetSuspendeAt_toNull() throws BlogException {
        FoundCancelledOnlySubscriptionProviderSpy subscriptionProvider = new FoundCancelledOnlySubscriptionProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);

        LocalDateTime beforeSubscribe = LocalDateTime.now();
        Subscription subscription = subscribe.execute(1, 1);
        LocalDateTime afterSubscribe = LocalDateTime.now();

        Course course = subscription.getCourse();
        assertThat(course.getEnrolledCount()).isEqualTo(1);
        assertThat(courseProvider.enrollCalledCount).isTrue();

        assertThat(course.getLessonsCount()).isEqualTo(2);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();

        Assertions.assertThat(subscription.getSuspendedAt()).isNull();
        Assertions.assertThat(subscription.isActive()).isTrue();
        Assertions.assertThat(subscription.getLastModifiedAt()).isAfter(subscription.getSubscribedAt());
        Assertions.assertThat(subscription.getLastModifiedAt()).isAfterOrEqualTo(beforeSubscribe);
        Assertions.assertThat(subscription.getLastModifiedAt()).isBeforeOrEqualTo(afterSubscribe);
    }

    @Test
    void executeDoesNothingIfUserAlreadySubscribed() throws BlogException {
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);

        Subscription subscription = subscribe.execute(1, 1);

        assertThat(subscriptionProvider.subscriptionOfCalled).isTrue();
        assertThat(subscriptionProvider.saveCalled).isFalse();

        Assertions.assertThat(subscription.isActive()).isTrue();


    }
}