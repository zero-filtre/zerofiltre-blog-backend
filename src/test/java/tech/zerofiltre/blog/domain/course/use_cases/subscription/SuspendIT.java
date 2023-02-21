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
@Import({DBSubscriptionProvider.class, DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBCourseProvider.class})
class SuspendIT {

    Suspend suspend;

    Subscribe subscribe;

    @Autowired
    SubscriptionProvider subscriptionProvider;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    ChapterProvider chapterProvider;

    @BeforeEach
    void init() {
        suspend = new Suspend(subscriptionProvider, dbCourseProvider, chapterProvider);
        subscribe = new Subscribe(subscriptionProvider, dbCourseProvider, dbUserProvider, chapterProvider);
    }

    @Test
    void suspendSavesSubscriptionProperly() throws BlogException, InterruptedException {
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        User subscriber = ZerofiltreUtils.createMockUser(false);
        subscriber.setEmail("test@gmail.grok");
        subscriber.setPseudoName("tester");
        subscriber = dbUserProvider.save(subscriber);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);
        subscribe.execute(subscriber.getId(), course.getId());
        LocalDateTime beforeSuspend = LocalDateTime.now();
        Subscription subscription = suspend.execute(subscriber.getId(), course.getId());
        LocalDateTime afterSuspend = LocalDateTime.now();

        assertThat(subscription).isNotNull();
        assertThat(subscription.getSubscriber().getId()).isEqualTo(subscriber.getId());
        assertThat(subscription.getSubscriber().getEmail()).isEqualTo(subscriber.getEmail());
        assertThat(subscription.getSubscriber().getPseudoName()).isEqualTo(subscriber.getPseudoName());
        assertThat(subscription.getCourse().getId()).isEqualTo(course.getId());
        assertThat(subscription.getCourse().getEnrolledCount()).isZero();

        assertThat(subscription.getId()).isNotZero();

        org.assertj.core.api.Assertions.assertThat(subscription.getSuspendedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(subscription.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        org.assertj.core.api.Assertions.assertThat(subscription.getSubscribedAt()).isBefore(subscription.getSuspendedAt());
        org.assertj.core.api.Assertions.assertThat(subscription.getSuspendedAt()).isBeforeOrEqualTo(afterSuspend);
        Assertions.assertThat(subscription.isActive()).isFalse();
    }
}
