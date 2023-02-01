package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;

import java.time.*;

public class Suspend {
    private final SubscriptionProvider subscriptionProvider;
    private final CourseProvider courseProvider;

    public Suspend(SubscriptionProvider subscriptionProvider, CourseProvider courseProvider) {
        this.subscriptionProvider = subscriptionProvider;
        this.courseProvider = courseProvider;
    }

    public Subscription execute(long userId, long courseId) throws ForbiddenActionException {
        Subscription subscription = subscriptionProvider.subscriptionOf(userId, courseId, true)
                .orElseThrow(() -> new ForbiddenActionException("You are not subscribed to the course of id " + courseId, Domains.COURSE.name()));
        subscription.setActive(false);
        subscription.setSuspendedAt(LocalDateTime.now());
        Subscription result = subscriptionProvider.save(subscription);

        Course resultCourse = result.getCourse();
        resultCourse.setEnrolledCount(courseProvider.getEnrolledCount(resultCourse.getId()));
        return result;


    }
}
