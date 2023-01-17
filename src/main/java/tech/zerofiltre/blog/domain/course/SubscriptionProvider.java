package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface SubscriptionProvider {

    void delete(long userId, long courseId);

    List<Subscription> subscriptionsOf(long userId);

    Optional<Subscription> subscriptionOf(long userId, long courseId);

    Subscription save(Subscription subscription);
}
