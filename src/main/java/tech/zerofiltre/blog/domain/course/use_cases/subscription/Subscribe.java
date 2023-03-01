package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import lombok.extern.slf4j.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

@Slf4j
public class Subscribe {

    private final SubscriptionProvider subscriptionProvider;
    private final CourseProvider courseProvider;
    private final UserProvider userProvider;
    private final ChapterProvider chapterProvider;

    public Subscribe(SubscriptionProvider subscriptionProvider, CourseProvider courseProvider, UserProvider userProvider, ChapterProvider chapterProvider) {
        this.subscriptionProvider = subscriptionProvider;
        this.courseProvider = courseProvider;
        this.userProvider = userProvider;
        this.chapterProvider = chapterProvider;
    }

    public Subscription execute(long userId, long courseId) throws BlogException {

        User user = userProvider.userOfId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We could not find the user with id " + userId,
                        String.valueOf(userId),
                        Domains.USER.name()));

        Course course = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We couldn't find the course of id " + courseId + " you are trying to subscribe to",
                        String.valueOf(courseId),
                        Domains.COURSE.name()));

        if (course.getStatus().compareTo(Status.PUBLISHED) < 0)
            throw new ForbiddenActionException("You can not subscribe to an unpublished course", Domains.COURSE.name());

        Subscription subscription = new Subscription();
        LocalDateTime lastModifiedAt = LocalDateTime.now();
        Optional<Subscription> existingSubscription = subscriptionProvider.subscriptionOf(userId, courseId, false);
        if (existingSubscription.isPresent()) {
            subscription = existingSubscription.get();
            subscription.setActive(true);
            subscription.setSuspendedAt(null);
        } else {
            subscription.setCourse(course);
            subscription.setSubscriber(user);
            lastModifiedAt = subscription.getSubscribedAt();
        }
        subscription.setLastModifiedAt(lastModifiedAt);
        Subscription result = subscriptionProvider.save(subscription);
        Course resultCourse = result.getCourse();
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        log.info("User {} subscribed to course {}", userId, courseId);
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
