package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;

public class CompleteLesson {

    private final SubscriptionProvider subscriptionProvider;
    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;

    public CompleteLesson(SubscriptionProvider subscriptionProvider, LessonProvider lessonProvider, ChapterProvider chapterProvider) {
        this.subscriptionProvider = subscriptionProvider;
        this.lessonProvider = lessonProvider;
        this.chapterProvider = chapterProvider;
    }

    public Subscription execute(long courseId, long lessonId, long currentUserId, boolean completeLesson) throws ResourceNotFoundException, ForbiddenActionException {
        Subscription existingSubscription = subscriptionProvider.subscriptionOf(currentUserId, courseId, true)
                .orElseThrow(() -> new ResourceNotFoundException("There is no subscription regarding the courseId and userId you submit", "Course Id = " + courseId + " " + "UserId = " + currentUserId, Domains.COURSE.name()));

        if (existingSubscription.getCompletedLessons().stream().anyMatch(lesson -> lesson.getId() == lessonId) == completeLesson && completeLesson)
            return existingSubscription;

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

        return subscriptionProvider.save(existingSubscription);
    }


}
