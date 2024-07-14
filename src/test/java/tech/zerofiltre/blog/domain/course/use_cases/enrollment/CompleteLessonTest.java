package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.CompletedLesson;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.doubles.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


class CompleteLessonTest {

    CompleteLesson completeLesson;


    @Test
    void completeLesson_ThrowsResourceNotFoundException_IfEnrollmentNotFound() {
        //given
        EnrollmentProvider enrollmentProvider = new NotEnrolledEnrollmentProvider();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
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
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

    @Test
    void completeLesson_returns_existingEnrollment_ifLessonAlreadyCompleted() throws ZerofiltreException {
        //given
        AlreadyCompletedLessonEnrollmentProvider enrollmentProvider = new AlreadyCompletedLessonEnrollmentProvider();
        FoundLessonProviderSpy lessonProvider = new FoundLessonProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);

        //when
        Enrollment enrollment = completeLesson.execute(1, 3, 1, true);


        //then
        assertThat(enrollment.getId()).isEqualTo(224);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();
        assertThat(courseProvider.enrollCalledCount).isTrue();
    }

    @Test
    void completeLesson_builds_and_adds_toCompletedLessons_Properly() throws ZerofiltreException {
        //given
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        FoundLessonProviderSpy lessonProvider = new FoundLessonProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        completeLesson = new CompleteLesson(enrollmentProviderSpy, lessonProvider, chapterProvider, courseProvider);
        LocalDateTime beforeCompletion = LocalDateTime.now();

        //when
        Enrollment enrollment = completeLesson.execute(1, 3, 1, true);

        //then
        List<CompletedLesson> completedLessons = enrollment.getCompletedLessons();
        assertThat(completedLessons).isNotEmpty();
        assertThat(lessonProvider.calledLessonId).isEqualTo(3);
        assertThat(chapterProvider.calledChapterId).isEqualTo(lessonProvider.lessonOfId(3).get().getChapterId());

        completedLessons.forEach(completed -> {
            assertThat(completed.getLessonId()).isEqualTo(1);
            assertThat(completed.getEnrollmentId()).isEqualTo(enrollment.getId());
            assertThat(completed.getCompletedAt()).isNotNull();
            assertThat(completed.getCompletedAt()).isAfterOrEqualTo(beforeCompletion);
        });
        assertThat(enrollmentProviderSpy.saveCalled).isTrue();
    }

    @Test
    void unCompleteLesson_removesLesson_fromCompletedLessons() throws ZerofiltreException {
        //given
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        LessonProvider lessonProvider = new FoundLessonProviderSpy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        completeLesson = new CompleteLesson(enrollmentProviderSpy, lessonProvider, chapterProvider, courseProvider);

        //when
        Enrollment enrollment = completeLesson.execute(1, 3, 1, true);
        List<CompletedLesson> completedLessons = enrollment.getCompletedLessons();
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
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
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
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        completeLesson = new CompleteLesson(enrollmentProviderSpy, lessonProvider, chapterProvider, courseProvider);

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> completeLesson.execute(1, 1, 1, true));

    }

}