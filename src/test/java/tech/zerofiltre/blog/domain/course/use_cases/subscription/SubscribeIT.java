package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;


@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBSubscriptionProvider.class})
class SubscribeIT {


    Subscribe subscribe;
    Suspend suspend;

    @Autowired
    SubscriptionProvider subscriptionProvider;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @BeforeEach
    void init() {
        subscribe = new Subscribe(subscriptionProvider, dbCourseProvider, dbUserProvider);
        suspend = new Suspend(subscriptionProvider);
    }

    @Test
    void subscribeGetsExecutedProperly() throws ForbiddenActionException, ResourceNotFoundException {
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        User subscriber = ZerofiltreUtils.createMockUser(false);
        subscriber.setEmail("test@gmail.grok");
        subscriber.setPseudoName("tester");
        subscriber = dbUserProvider.save(subscriber);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);
        LocalDateTime beforeSubscribe = LocalDateTime.now();
        Subscription subscription = subscribe.execute(subscriber.getId(), course.getId());
        LocalDateTime afterSubscribe = LocalDateTime.now();

        assertThat(subscription).isNotNull();
        assertThat(subscription.getSubscriber().getId()).isEqualTo(subscriber.getId());
        assertThat(subscription.getSubscriber().getEmail()).isEqualTo(subscriber.getEmail());
        assertThat(subscription.getSubscriber().getPseudoName()).isEqualTo(subscriber.getPseudoName());
        assertThat(subscription.getCourse().getId()).isEqualTo(course.getId());

        assertThat(subscription.getId()).isNotZero();
        assertThat(subscription.isCompleted()).isFalse();
        Assertions.assertThat(subscription.getCompletedLessons()).isEmpty();

        org.assertj.core.api.Assertions.assertThat(subscription.getSubscribedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(subscription.getSubscribedAt()).isAfterOrEqualTo(beforeSubscribe);
        Assertions.assertThat(subscription.getSubscribedAt()).isBeforeOrEqualTo(afterSubscribe);
        Assertions.assertThat(subscription.isActive()).isTrue();
    }


    @Test
    void executeSetSuspendeAt_toNull() throws ResourceNotFoundException, ForbiddenActionException {
        User subscriber = ZerofiltreUtils.createMockUser(false);
        subscriber.setEmail("test@gmail.grok");
        subscriber.setPseudoName("tester");
        subscriber = dbUserProvider.save(subscriber);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, subscriber, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);

        subscribe.execute(subscriber.getId(), course.getId());

        Subscription suspendedSubscription = suspend.execute(subscriber.getId(), course.getId());
        assertThat(suspendedSubscription.getSuspendedAt()).isNotNull();

        Subscription subscription = subscribe.execute(subscriber.getId(), course.getId());

        Assertions.assertThat(subscription.getSuspendedAt()).isNull();
        Assertions.assertThat(subscription.getId()).isEqualTo(suspendedSubscription.getId());

    }
}
