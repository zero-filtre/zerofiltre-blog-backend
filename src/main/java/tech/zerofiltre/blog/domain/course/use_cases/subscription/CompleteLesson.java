package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;

public class CompleteLesson {

    private final SubscriptionProvider subscriptionProvider;
    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;
    private final CourseProvider courseProvider;

    public CompleteLesson(SubscriptionProvider subscriptionProvider, LessonProvider lessonProvider, ChapterProvider chapterProvider, CourseProvider courseProvider) {
        this.subscriptionProvider = subscriptionProvider;
        this.lessonProvider = lessonProvider;
        this.chapterProvider = chapterProvider;
        this.courseProvider = courseProvider;
    }

    public Subscription execute(long courseId, long lessonId, long currentUserId, boolean completeLesson) throws BlogException {
        Subscription existingSubscription = subscriptionProvider.subscriptionOf(currentUserId, courseId, true)
                .orElseThrow(() -> new ResourceNotFoundException("There is no subscription regarding the courseId and userId you submit", "Course Id = " + courseId + " " + "UserId = " + currentUserId, Domains.COURSE.name()));

        if (existingSubscription.getCompletedLessons().stream().anyMatch(lesson -> lesson.getId() == lessonId) == completeLesson && completeLesson)
            return computeCounts(existingSubscription);

        Lesson lesson = lessonProvider.lessonOfId(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson of id " + lessonId + " does not exist", String.valueOf(lessonId), Domains.COURSE.name()));

        ForbiddenActionException forbiddenActionException = new ForbiddenActionException("Lesson not part of this subscription", Domains.COURSE.name());

        Chapter chapter = chapterProvider.chapterOfId(lesson.getChapterId())
                .orElseThrow(() -> forbiddenActionException);

        if (chapter.getCourseId() != courseId) throw forbiddenActionException;

        if (completeLesson) {
            existingSubscription.getCompletedLessons().add(lesson);
        } else {
            existingSubscription.getCompletedLessons().removeIf(existingLesson -> existingLesson.getId() == lesson.getId());
        }

        Subscription result = subscriptionProvider.save(existingSubscription);
        return computeCounts(result);
    }

    private Subscription computeCounts(Subscription result) {
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
