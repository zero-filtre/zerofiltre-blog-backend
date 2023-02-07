package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.domain.FinderRequest.Filter.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;


class FindSubscriptionTest {

    @Test
    void findSubscription_returns_theProperPage() {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindSubscription findSubscription = new FindSubscription(subscriptionProvider, courseProvider, chapterProvider);
        //when
        FinderRequest request = new FinderRequest(0, 3, DRAFT, new User());
        Page<Course> courses = findSubscription.of(request);

        //then
        assertThat(subscriptionProvider.ofCalled).isTrue();
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(2);
        assertThat(courses.getHasNext()).isTrue();
        assertThat(courses.getHasPrevious()).isTrue();
        assertThat(courses.getNumberOfElements()).isEqualTo(2);
        assertThat(courses.getPageNumber()).isEqualTo(1);
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(10);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(4);
        courses.getContent().forEach(course -> {
            assertThat(course.getEnrolledCount()).isEqualTo(1);
            assertThat(course.getLessonsCount()).isEqualTo(2);
        });
    }

    @Test
    void findSubscription_calls_SubscriptionProvider_withTheInactiveParam() {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindSubscription findSubscription = new FindSubscription(subscriptionProvider, courseProvider, chapterProvider);
        //when
        FinderRequest request = new FinderRequest(0, 3, DRAFT, new User());
        request.setFilter(INACTIVE);
        findSubscription.of(request);

        //then
        assertThat(subscriptionProvider.ofCalled).isTrue();
        assertThat(subscriptionProvider.ofFilter).isNotNull();
        assertThat(subscriptionProvider.ofFilter).isEqualTo(INACTIVE);
    }

    @Test
    void findSubscription_calls_SubscriptionProvider_withTheCompletedParam() {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindSubscription findSubscription = new FindSubscription(subscriptionProvider, courseProvider, chapterProvider);
        //when
        FinderRequest request = new FinderRequest(0, 3, DRAFT, new User());
        request.setFilter(COMPLETED);
        findSubscription.of(request);

        //then
        assertThat(subscriptionProvider.ofCalled).isTrue();
        assertThat(subscriptionProvider.ofFilter).isNotNull();
        assertThat(subscriptionProvider.ofFilter).isEqualTo(COMPLETED);
    }

    @Test
    void findASubscription_returns_theProperOne() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindSubscription findSubscription = new FindSubscription(subscriptionProvider, courseProvider, chapterProvider);
        User executor = ZerofiltreUtils.createMockUser(true);
        //when
        Subscription subscription = findSubscription.of(0, 1, executor);
        //then
        assertThat(subscription).isNotNull();

    }

    @Test
    void findASubscription_throwsResourceNotFoundException() {
        //given
        SubscriptionProvider subscriptionProvider = new NotFoundSubscriptionProviderDummy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindSubscription findSubscription = new FindSubscription(subscriptionProvider, courseProvider, chapterProvider);
        User executor = ZerofiltreUtils.createMockUser(true);

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> findSubscription.of(1, 1, executor))
                .withMessage("Subscription not found");
    }

    @Test
    void findASubscription_throwsForbiddenActionException_ifExecutor_isNotAdmin_NorInvolved() {
        //given
        SubscriptionProvider subscriptionProvider = new SubscriptionProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindSubscription findSubscription = new FindSubscription(subscriptionProvider, courseProvider, chapterProvider);
        User executor = ZerofiltreUtils.createMockUser(false);

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> findSubscription.of(1, 1, executor))
                .withMessage("You are only allow to look for your subscriptions");
    }

}