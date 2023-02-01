package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.doubles.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;


class CompleteLessonTest {

    CompleteLesson completeLesson;


    @Test
    void completeLesson_ThrowsResourceNotFoundException_IfSubscriptionNotFound() {
        //given
        SubscriptionProvider subscriptionProvider = new NotSubscribedSubscriptionProvider();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, false));

    }

    @Test
    void completeLesson_ThrowsResourceNotFoundException_IfLessonNotFound() {
        //given
        SubscriptionProvider subscriptionProvider = new NotSubscribedSubscriptionProvider();
        LessonProvider lessonProvider = new NotFoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();

        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

    @Test
    void completeLesson_returns_existingSubscription_ifLessonAlreadyCompleted() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        AlreadyCompletedLessonSubscriptionProvider subscriptionProvider = new AlreadyCompletedLessonSubscriptionProvider();
        FoundLessonProviderSpy lessonProvider = new FoundLessonProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();

        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);

        //when
        Subscription subscription = completeLesson.execute(1, 3, 1, true);

        //then
        assertThat(subscription.getId()).isEqualTo(224);
    }

    @Test
    void completeLesson_addLessonId_toCompletedLessons() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        FoundLessonProviderSpy lessonProvider = new FoundLessonProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();

        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);

        //when
        Subscription subscription = completeLesson.execute(1, 3, 1, true);

        //then
        List<Lesson> completedLessons = subscription.getCompletedLessons();
        assertThat(completedLessons).isNotEmpty();
        assertThat(lessonProvider.calledLessonId).isEqualTo(3);
        assertThat(chapterProvider.calledChapterId).isEqualTo(lessonProvider.lessonOfId(3).get().getChapterId());
        completedLessons.forEach(lesson -> assertThat(lesson.getId()).isEqualTo(1));
        assertThat(subscriptionProvider.saveCalled).isTrue();
    }

    @Test
    void unCompleteLesson_removesLessonId_fromCompletedLessons() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();

        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);

        //when
        Subscription subscription = completeLesson.execute(1, 3, 1, true);
        List<Lesson> completedLessons = subscription.getCompletedLessons();
        assertThat(completedLessons).isNotEmpty();


        subscription = completeLesson.execute(1, 3, 1, false);
        completedLessons = subscription.getCompletedLessons();
        assertThat(completedLessons).isEmpty();

        //then
        assertThat(subscriptionProvider.saveCalled).isTrue();
    }

    @Test
    @DisplayName("CompleteLesson throws ForbiddenActionException when chapter " +
            "related to the lesson is not found, meaning the lesson is not part of the subscription")
    void completeLesson_throws_ForbiddenActionException_when_chapterNotFound() {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();

        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

    @Test
    @DisplayName("CompleteLesson throws ForbiddenActionException when the found chapter " +
            "does not belong to the considered course, meaning the lesson is not part of the subscription")
    void completeLesson_throws_ForbiddenActionException_when_lesson_NotPart_of_Subscription() {
        //given
        SubscriptionProviderSpy subscriptionProvider = new SubscriptionProviderSpy();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterWithUnknownCourseProviderSpy();

        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

}