package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

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
    void completeLesson_ThrowsResourceNotFoundException_IfEnrollmentNotFound() {
        //given
        EnrollmentProvider enrollmentProvider = new NotEnrolledEnrollmentProvider();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, false));

    }

    @Test
    void completeLesson_ThrowsResourceNotFoundException_IfLessonNotFound() {
        //given
        EnrollmentProvider enrollmentProvider = new NotEnrolledEnrollmentProvider();
        LessonProvider lessonProvider = new NotFoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

    @Test
    void completeLesson_returns_existingEnrollment_ifLessonAlreadyCompleted() throws BlogException {
        //given
        AlreadyCompletedLessonEnrollmentProvider enrollmentProvider = new AlreadyCompletedLessonEnrollmentProvider();
        FoundLessonProviderSpy lessonProvider = new FoundLessonProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);

        //when
        Enrollment enrollment = completeLesson.execute(1, 3, 1, true);


        //then
        assertThat(enrollment.getId()).isEqualTo(224);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();
        assertThat(courseProvider.enrollCalledCount).isTrue();
    }

    @Test
    void completeLesson_addLessonId_toCompletedLessons() throws BlogException {
        //given
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        FoundLessonProviderSpy lessonProvider = new FoundLessonProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        completeLesson = new CompleteLesson(enrollmentProviderSpy, lessonProvider, chapterProvider, courseProvider);

        //when
        Enrollment enrollment = completeLesson.execute(1, 3, 1, true);

        //then
        List<Lesson> completedLessons = enrollment.getCompletedLessons();
        assertThat(completedLessons).isNotEmpty();
        assertThat(lessonProvider.calledLessonId).isEqualTo(3);
        assertThat(chapterProvider.calledChapterId).isEqualTo(lessonProvider.lessonOfId(3).get().getChapterId());
        completedLessons.forEach(lesson -> assertThat(lesson.getId()).isEqualTo(1));
        assertThat(enrollmentProviderSpy.saveCalled).isTrue();
    }

    @Test
    void unCompleteLesson_removesLessonId_fromCompletedLessons() throws BlogException {
        //given
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        completeLesson = new CompleteLesson(enrollmentProviderSpy, lessonProvider, chapterProvider, courseProvider);

        //when
        Enrollment enrollment = completeLesson.execute(1, 3, 1, true);
        List<Lesson> completedLessons = enrollment.getCompletedLessons();
        assertThat(completedLessons).isNotEmpty();


        enrollment = completeLesson.execute(1, 3, 1, false);
        completedLessons = enrollment.getCompletedLessons();
        assertThat(completedLessons).isEmpty();

        //then
        assertThat(enrollmentProviderSpy.saveCalled).isTrue();
    }

    @Test
    @DisplayName("CompleteLesson throws ForbiddenActionException when chapter " +
            "related to the lesson is not found, meaning the lesson is not part of the enrollment")
    void completeLesson_throws_ForbiddenActionException_when_chapterNotFound() {
        //given
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        completeLesson = new CompleteLesson(enrollmentProviderSpy, lessonProvider, chapterProvider, courseProvider);

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

    @Test
    @DisplayName("CompleteLesson throws ForbiddenActionException when the found chapter " +
            "does not belong to the considered course, meaning the lesson is not part of the enrollment")
    void completeLesson_throws_ForbiddenActionException_when_lesson_NotPart_of_Enrollment() {
        //given
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterWithUnknownCourseProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        completeLesson = new CompleteLesson(enrollmentProviderSpy, lessonProvider, chapterProvider, courseProvider);

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

}