package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import lombok.extern.slf4j.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;

import java.time.*;

@Slf4j
public class Suspend {
    private final SubscriptionProvider subscriptionProvider;
    private final CourseProvider courseProvider;
    private final ChapterProvider chapterProvider;

    public Suspend(SubscriptionProvider subscriptionProvider, CourseProvider courseProvider, ChapterProvider chapterProvider) {
        this.subscriptionProvider = subscriptionProvider;
        this.courseProvider = courseProvider;
        this.chapterProvider = chapterProvider;
    }

    public Subscription execute(long userId, long courseId) throws BlogException {
        Subscription subscription = subscriptionProvider.subscriptionOf(userId, courseId, true)
                .orElseThrow(() -> new ForbiddenActionException("You are not subscribed to the course of id " + courseId, Domains.COURSE.name()));
        subscription.setActive(false);
        subscription.setSuspendedAt(LocalDateTime.now());
        Subscription result = subscriptionProvider.save(subscription);

        Course resultCourse = result.getCourse();
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        log.info("User {} subscription suspended for course {}", userId, courseId);
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
