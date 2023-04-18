package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;

public class CompleteLesson {

    private final EnrollmentProvider enrollmentProvider;
    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;
    private final CourseProvider courseProvider;

    public CompleteLesson(EnrollmentProvider enrollmentProvider, LessonProvider lessonProvider, ChapterProvider chapterProvider, CourseProvider courseProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.lessonProvider = lessonProvider;
        this.chapterProvider = chapterProvider;
        this.courseProvider = courseProvider;
    }

    public Enrollment execute(long courseId, long lessonId, long currentUserId, boolean completeLesson) throws BlogException {
        Enrollment existingEnrollment = enrollmentProvider.enrollmentOf(currentUserId, courseId, true)
                .orElseThrow(() -> new ResourceNotFoundException("There is no enrollment regarding the courseId and userId you submit", "Course Id = " + courseId + " " + "UserId = " + currentUserId, Domains.COURSE.name()));

        if (existingEnrollment.getCompletedLessons().stream().anyMatch(lesson -> lesson.getId() == lessonId) == completeLesson && completeLesson)
            return computeCounts(existingEnrollment);

        Lesson lesson = lessonProvider.lessonOfId(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson of id " + lessonId + " does not exist", String.valueOf(lessonId), Domains.COURSE.name()));

        ForbiddenActionException forbiddenActionException = new ForbiddenActionException("Lesson not part of this enrollment", Domains.COURSE.name());

        Chapter chapter = chapterProvider.chapterOfId(lesson.getChapterId())
                .orElseThrow(() -> forbiddenActionException);

        if (chapter.getCourseId() != courseId) throw forbiddenActionException;

        if (completeLesson) {
            existingEnrollment.getCompletedLessons().add(lesson);
        } else {
            existingEnrollment.getCompletedLessons().removeIf(existingLesson -> existingLesson.getId() == lesson.getId());
        }

        Enrollment result = enrollmentProvider.save(existingEnrollment);
        return computeCounts(result);
    }

    private Enrollment computeCounts(Enrollment result) {
        Course resultCourse = result.getCourse();
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        return result;
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }


    private int getEnrolledCount(long courseId) {
        return courseProvider.getEnrolledCount(courseId);
    }

}
