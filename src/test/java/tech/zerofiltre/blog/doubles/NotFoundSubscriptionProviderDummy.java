package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class NotFoundSubscriptionProviderDummy implements SubscriptionProvider {

    public boolean saveCalled;

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
    public Page<Subscription> of(int pageNumber, int pageSize, long authorId, FinderRequest.Filter filter, String tag) {
        return null;
    }


    @Override
    public Optional<Subscription> subscriptionOf(long userId, long courseId, boolean isActive) {
        return Optional.empty();
    }
}
