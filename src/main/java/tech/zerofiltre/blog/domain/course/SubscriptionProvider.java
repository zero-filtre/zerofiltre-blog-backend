package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;

import java.util.*;

public interface SubscriptionProvider {

    void delete(long userId, long courseId);

    Page<Subscription> of(int pageNumber, int pageSize, long authorId, FinderRequest.Filter filter, String tag);

    Optional<Subscription> subscriptionOf(long userId, long courseId, boolean isActive);

    Subscription save(Subscription subscription) throws BlogException;

}
