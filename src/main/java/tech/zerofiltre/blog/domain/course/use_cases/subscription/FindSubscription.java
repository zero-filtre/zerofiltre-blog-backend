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

    public FindSubscription(SubscriptionProvider subscriptionProvider) {
        this.subscriptionProvider = subscriptionProvider;
    }

    public Page<Course> of(FinderRequest request) {
        Page<Subscription> subscriptions = subscriptionProvider.of(request.getPageNumber(), request.getPageSize(), request.getUser().getId(), request.getFilter(), request.getTag());
        List<Course> courses = subscriptions.getContent().stream().map(Subscription::getCourse).collect(Collectors.toList());
        Page<Course> result = new Page<>();
        result.setPageSize(subscriptions.getPageSize());
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
        return subscriptionProvider.subscriptionOf(userId, courseId, true).orElseThrow(
                () -> new ResourceNotFoundException("Subscription not found", courseId + "/" + userId, null));
    }
}
