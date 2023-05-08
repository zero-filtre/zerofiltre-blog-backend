package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

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
class FindEnrollmentIT {

    private FindEnrollment findEnrollment;

    @Autowired
    private EnrollmentJPARepository enrollmentJPARepository;

    @Autowired
    private UserJPARepository userJPARepository;

    @Autowired
    private CourseJPARepository courseJPARepository;
    @Autowired
    private ChapterJPARepository chapterJPARepository;

    @Test
    void findEnrollment_returns_properPage_forInActiveEnrollments() throws ZerofiltreException {
        //given
        User user = init2Enrollments(true, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        request.setFilter(FinderRequest.Filter.INACTIVE);
        Page<Course> courses = findEnrollment.of(request);

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
    void findEnrollment_returns_properPage_forActiveEnrollments() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        Page<Course> courses = findEnrollment.of(request);

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
    void findEnrollment_returns_completedAndActiveEnrollments() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, true);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        request.setFilter(FinderRequest.Filter.COMPLETED);
        Page<Course> courses = findEnrollment.of(request);

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
    void findEnrollment_returns_nothingOnNotMatch() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        request.setFilter(FinderRequest.Filter.COMPLETED);
        Page<Course> courses = findEnrollment.of(request);

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
    void findEnrollment_returns_uncompletedAndActiveEnrollment() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        Page<Course> courses = findEnrollment.of(request);

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

    private User init2Enrollments(boolean withThe2ndOneInactive, boolean withThe2ndOneCompleted) throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = new DBEnrollmentProvider(enrollmentJPARepository);
        UserProvider userProvider = new DBUserProvider(userJPARepository);
        CourseProvider courseProvider = new DBCourseProvider(courseJPARepository);
        ChapterProvider chapterProvider = new DBChapterProvider(chapterJPARepository);
        findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);


        User author = ZerofiltreUtils.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        User user = ZerofiltreUtils.createMockUser(false);
        user.setPseudoName("enrolled");
        user.setEmail("susbscriber@gamil.fr");


        author = userProvider.save(author);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);
        Course susPendedCourse = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        susPendedCourse = courseProvider.save(susPendedCourse);

        Enrollment enrollment = ZerofiltreUtils.createMockEnrollment(false, user, course);
        enrollmentProvider.save(enrollment);

        Enrollment sencondEnrollment = ZerofiltreUtils.createMockEnrollment(false, user, susPendedCourse);
        sencondEnrollment.setActive(!withThe2ndOneInactive);
        sencondEnrollment.setCompleted(withThe2ndOneCompleted);
        enrollmentProvider.save(sencondEnrollment);
        return user;
    }
}
