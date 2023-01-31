package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBUserProvider.class, DBCourseProvider.class, DBSubscriptionProvider.class})
class DBSubscriptionProviderIT {

    @Autowired
    private DBSubscriptionProvider subscriptionProvider;
    @Autowired
    private DBUserProvider userProvider;
    @Autowired
    private DBCourseProvider courseProvider;


    @Test
    void of_returns_ProperPageOfSubscription() {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Course course1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course1 = courseProvider.save(course1);

        Subscription subscription = new Subscription();
        subscription.setCourse(course);
        subscription.setSubscriber(user);

        Subscription subscription1 = new Subscription();
        subscription1.setCourse(course1);
        subscription1.setSubscriber(user);

        subscriptionProvider.save(subscription);
        subscriptionProvider.save(subscription1);

        //when
        Page<Subscription> result = subscriptionProvider.of(0, 2, user.getId(), null, null);


        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(2);
        assertThat(result.getTotalNumberOfElements()).isEqualTo(2);
        assertThat(result.getTotalNumberOfPages()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getHasNext()).isFalse();

    }

    @Test
    void of_does_not_return_inactive_subscriptions() {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Course course1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course1 = courseProvider.save(course1);

        Subscription subscription = new Subscription();
        subscription.setCourse(course);
        subscription.setSubscriber(user);

        Subscription subscription1 = new Subscription();
        subscription1.setCourse(course1);
        subscription1.setSubscriber(user);

        subscriptionProvider.save(subscription);
        subscription1 = subscriptionProvider.save(subscription1);
        subscription1.setActive(false);
        subscriptionProvider.save(subscription1);

        //when
        Page<Subscription> result = subscriptionProvider.of(0, 2, user.getId(), null, null);


        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getTotalNumberOfElements()).isEqualTo(1);

    }
}
