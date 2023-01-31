package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

public class SubscriptionProviderSpy implements SubscriptionProvider {

    public boolean saveCalled = false;
    public boolean ofCalled = false;

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
        User mockUser = ZerofiltreUtils.createMockUser(false);
        Course mockCourse2 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, mockUser,
                Collections.emptyList(), Collections.emptyList());
        Course mockCourse1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, mockUser,
                Collections.emptyList(), Collections.emptyList());

        Subscription subscription = new Subscription();
        subscription.setId(1);
        subscription.setCourse(mockCourse1);
        subscription.setSubscriber(mockUser);

        Subscription subscription2 = new Subscription();
        subscription2.setId(2);
        subscription2.setCourse(mockCourse2);
        subscription2.setSubscriber(mockUser);
        Page<Subscription> result = new Page<>();

        result.setContent(Arrays.asList(subscription, subscription2));
        result.setTotalNumberOfElements(10);
        result.setNumberOfElements(2);
        result.setTotalNumberOfPages(4);
        result.setPageNumber(1);
        result.setPageSize(2);
        result.setHasNext(true);
        result.setHasPrevious(true);

        ofCalled = true;
        return result;
    }


    @Override
    public Optional<Subscription> subscriptionOf(long userId, long courseId, boolean isActive) {
        Subscription value = new Subscription();
        value.setSubscribedAt(LocalDateTime.now().minusDays(2));
        value.setLastModifiedAt(value.getSubscribedAt());
        value.setSuspendedAt(LocalDateTime.now().minusDays(1));
        value.setActive(false);
        return Optional.of(value);
    }
}
