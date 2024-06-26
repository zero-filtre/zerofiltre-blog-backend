package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.PUBLISHED;

@DataJpaTest
class DBCourseProviderIT {

    DBCourseProvider courseProvider;


    DBUserProvider userProvider;

    @Autowired
    CourseJPARepository courseJPARepository;


    @Autowired
    UserJPARepository userJPARepository;

    @Autowired
    private EnrollmentJPARepository enrollmentJPARepository;

    @BeforeEach
    void init() {
        courseProvider = new DBCourseProvider(courseJPARepository);
        userProvider = new DBUserProvider(userJPARepository);
    }

    @Test
    void savingACourse_isOK() {
        User author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course.setMentored(true);
        course = courseProvider.save(course);

        assertThat(course.getId()).isNotZero();
        assertThat(course.isMentored()).isTrue();
    }

    @Test
    void getACourseByItsId_isOk() {
        User author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Optional<Course> courseOptional = courseProvider.courseOfId(course.getId());

        assertThat(courseOptional).isPresent();
        assertThat(courseOptional.get().getId()).isEqualTo(course.getId());
        assertThat(courseOptional.get().isMentored()).isFalse();
    }

    @Test
    void getEnrolledCount_GetsEnrollments_ActiveAndNotActive() throws ZerofiltreException {
        //given
        Course course = init2Enrollments(true, false);

        //when
        long enrolledCount = courseProvider.getEnrolledCount(course.getId());

        //then
        assertThat(enrolledCount).isEqualTo(2);


    }

    @Test
    void getEnrolledCount_GetsEnrollments_CompletedAndNotCompleted() throws ZerofiltreException {
        //given
        Course course = init2Enrollments(true, true);

        //when
        long enrolledCount = courseProvider.getEnrolledCount(course.getId());

        //then
        assertThat(enrolledCount).isEqualTo(2);


    }

    private Course init2Enrollments(boolean withThe2ndOneInactive, boolean withThe2ndOneCompleted) throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = new DBEnrollmentProvider(enrollmentJPARepository);
        UserProvider userProvider = new DBUserProvider(userJPARepository);
        CourseProvider courseProvider = new DBCourseProvider(courseJPARepository);


        User author = ZerofiltreUtils.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        User user = ZerofiltreUtils.createMockUser(false);
        user.setPseudoName("enrolled");
        user.setEmail("susbscriber@gamil.fr");

        User secondUser = ZerofiltreUtils.createMockUser(false);
        secondUser.setPseudoName("enrolledSecond");
        secondUser.setEmail("susbscriberSecond@gamil.fr");


        author = userProvider.save(author);
        user = userProvider.save(user);
        secondUser = userProvider.save(secondUser);

        Course course = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Enrollment enrollment = ZerofiltreUtils.createMockEnrollment(false, user, course);
        enrollmentProvider.save(enrollment);

        Enrollment sencondEnrollment = ZerofiltreUtils.createMockEnrollment(false, secondUser, course);
        sencondEnrollment.setActive(!withThe2ndOneInactive);
        sencondEnrollment.setCompleted(withThe2ndOneCompleted);
        enrollmentProvider.save(sencondEnrollment);
        return course;
    }
}
