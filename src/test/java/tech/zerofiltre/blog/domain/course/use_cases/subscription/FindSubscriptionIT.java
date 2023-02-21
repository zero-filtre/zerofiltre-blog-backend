package tech.zerofiltre.blog.domain.course.use_cases.subscription;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@DataJpaTest
class FindSubscriptionIT {

    private FindSubscription findSubscription;

    @Autowired
    private SubscriptionJPARepository subscriptionJPARepository;

    @Autowired
    private UserJPARepository userJPARepository;

    @Autowired
    private CourseJPARepository courseJPARepository;
    @Autowired
    private ChapterJPANumberRepository cJPANumberRepository;
    @Autowired
    private ChapterJPARepository chapterJPARepository;
    @Autowired
    private ChapterJPANumberRepository chapterJPANumberRepository;

    @Test
    void findSubscription_returns_properPage_forInActiveSubscriptions() throws BlogException {
        //given
        User subscriber = init2Subscriptions(true, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, subscriber);
        request.setFilter(FinderRequest.Filter.INACTIVE);
        Page<Course> courses = findSubscription.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(1);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(1);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(1);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    @Test
    void findSubscription_returns_properPage_forActiveSubscriptions() throws BlogException {
        //given
        User subscriber = init2Subscriptions(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, subscriber);
        Page<Course> courses = findSubscription.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(2);

        courses.getContent().forEach(course -> assertThat(course.getEnrolledCount()).isOne());

        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(2);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    @Test
    void findSubscription_returns_completedAndActiveSubscription() throws BlogException {
        //given
        User subscriber = init2Subscriptions(false, true);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, subscriber);
        request.setFilter(FinderRequest.Filter.COMPLETED);
        Page<Course> courses = findSubscription.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(1);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(1);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(1);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    @Test
    void findSubscription_returns_nothingOnNotMatch() throws BlogException {
        //given
        User subscriber = init2Subscriptions(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, subscriber);
        request.setFilter(FinderRequest.Filter.COMPLETED);
        Page<Course> courses = findSubscription.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(0);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(0);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(0);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(0);
    }

    @Test
    void findSubscription_returns_uncompletedAndActiveSubscription() throws BlogException {
        //given
        User subscriber = init2Subscriptions(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, subscriber);
        Page<Course> courses = findSubscription.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(2);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(2);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    private User init2Subscriptions(boolean withThe2ndOneInactive, boolean withThe2ndOneCompleted) throws BlogException {
        SubscriptionProvider subscriptionProvider = new DBSubscriptionProvider(subscriptionJPARepository);
        UserProvider userProvider = new DBUserProvider(userJPARepository);
        CourseProvider courseProvider = new DBCourseProvider(courseJPARepository);
        ChapterProvider chapterProvider = new DBChapterProvider(chapterJPARepository, chapterJPANumberRepository);
        findSubscription = new FindSubscription(subscriptionProvider, courseProvider, chapterProvider);


        User author = ZerofiltreUtils.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        User subscriber = ZerofiltreUtils.createMockUser(false);
        subscriber.setPseudoName("subscriber");
        subscriber.setEmail("susbscriber@gamil.fr");


        author = userProvider.save(author);
        subscriber = userProvider.save(subscriber);

        Course course = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);
        Course susPendedCourse = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        susPendedCourse = courseProvider.save(susPendedCourse);

        Subscription subscription = ZerofiltreUtils.createMockSubscription(false, subscriber, course);
        subscriptionProvider.save(subscription);

        Subscription sencondSubscription = ZerofiltreUtils.createMockSubscription(false, subscriber, susPendedCourse);
        sencondSubscription.setActive(!withThe2ndOneInactive);
        sencondSubscription.setCompleted(withThe2ndOneCompleted);
        subscriptionProvider.save(sencondSubscription);
        return subscriber;
    }
}
