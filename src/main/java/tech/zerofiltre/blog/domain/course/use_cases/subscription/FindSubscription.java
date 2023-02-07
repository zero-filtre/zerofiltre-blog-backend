package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;
import java.util.stream.*;

public class FindSubscription {

    private final SubscriptionProvider subscriptionProvider;
    private final CourseProvider courseProvider;
    private final ChapterProvider chapterProvider;

    public FindSubscription(SubscriptionProvider subscriptionProvider, CourseProvider courseProvider, ChapterProvider chapterProvider) {
        this.subscriptionProvider = subscriptionProvider;
        this.courseProvider = courseProvider;
        this.chapterProvider = chapterProvider;
    }

    public Page<Course> of(FinderRequest request) {
        Page<Subscription> subscriptions = subscriptionProvider.of(request.getPageNumber(), request.getPageSize(), request.getUser().getId(), request.getFilter(), request.getTag());
        List<Course> courses = subscriptions.getContent().stream().map(Subscription::getCourse).collect(Collectors.toList());
        Page<Course> result = new Page<>();
        result.setPageSize(subscriptions.getPageSize());
        courses.forEach(course -> {
            course.setEnrolledCount(getEnrolledCount(course.getId()));
            course.setLessonsCount(getLessonsCount(course.getId()));
        });
        result.setContent(courses);
        result.setHasNext(subscriptions.getHasNext());
        result.setHasPrevious(subscriptions.getHasPrevious());
        result.setTotalNumberOfPages(subscriptions.getTotalNumberOfPages());
        result.setNumberOfElements(subscriptions.getNumberOfElements());
        result.setTotalNumberOfElements(subscriptions.getTotalNumberOfElements());
        result.setPageNumber(subscriptions.getPageNumber());
        return result;
    }

    public Subscription of(long courseId, long userId, User executor) throws ResourceNotFoundException, ForbiddenActionException {
        if (!executor.isAdmin() && executor.getId() != userId) {
            throw new ForbiddenActionException("You are only allow to look for your subscriptions", "");
        }
        return subscriptionProvider.subscriptionOf(userId, courseId, true)
                .map(subscription -> {
                    subscription.getCourse().setEnrolledCount(getEnrolledCount(courseId));
                    subscription.getCourse().setLessonsCount(getLessonsCount(courseId));
                    return subscription;
                }).orElseThrow(() -> new ResourceNotFoundException("Subscription not found", courseId + "/" + userId, null));
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }


    private int getEnrolledCount(long courseId) {
        return courseProvider.getEnrolledCount(courseId);
    }
}
