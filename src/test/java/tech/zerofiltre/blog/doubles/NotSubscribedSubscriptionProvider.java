package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class NotSubscribedSubscriptionProvider implements SubscriptionProvider {

    @Override
    public void delete(long userId, long courseId) {

    }

    @Override
    public List<Subscription> subscriptionsOf(long userId) {
        return null;
    }

    @Override
    public Optional<Subscription> subscriptionOf(long userId, long courseId) {
        return Optional.empty();
    }

    @Override
    public Subscription save(Subscription subscription) {
        return null;
    }
}
