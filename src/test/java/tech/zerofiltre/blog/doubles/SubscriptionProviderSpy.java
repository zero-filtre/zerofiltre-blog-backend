package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class SubscriptionProviderSpy implements SubscriptionProvider {

    public boolean saveCalled = false;

    @Override
    public Subscription save(Subscription subscription) {
        saveCalled = true;
        subscription.setId(1);
        return subscription;
    }

    @Override
    public void delete(long userId, long courseId) {

    }

    @Override
    public List<Subscription> subscriptionsOf(long userId) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Subscription> subscriptionOf(long userId, long courseId) {
        return Optional.of(new Subscription());
    }
}
