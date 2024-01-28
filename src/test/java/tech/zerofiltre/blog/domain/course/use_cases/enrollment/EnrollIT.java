package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBEnrollmentProvider.class, DBChapterProvider.class})
class EnrollIT {


    Enroll enroll;
    Suspend suspend;

    @Autowired
    EnrollmentProvider enrollmentProvider;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    ChapterProvider chapterProvider;

    @BeforeEach
    void init() {
        enroll = new Enroll(enrollmentProvider, dbCourseProvider, dbUserProvider, chapterProvider, null, null);
        suspend = new Suspend(enrollmentProvider, dbCourseProvider, chapterProvider, null);
    }

    @Test
    void enrollGetsExecutedProperly() throws ZerofiltreException {
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        User user = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        user.setEmail("test@gmail.grok");
        user.setPseudoName("tester");
        user = dbUserProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);
        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(user.getId(), course.getId(), true);
        LocalDateTime afterEnroll = LocalDateTime.now();

        assertThat(enrollment).isNotNull();
        Assertions.assertThat(enrollment.getPlan()).isEqualTo(User.Plan.PRO);
        assertThat(enrollment.getUser().getId()).isEqualTo(user.getId());
        assertThat(enrollment.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(enrollment.getUser().getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollment.getCourse().getEnrolledCount()).isOne();

        assertThat(enrollment.getId()).isNotZero();
        assertThat(enrollment.isCompleted()).isFalse();
        Assertions.assertThat(enrollment.getCompletedLessons()).isEmpty();

        org.assertj.core.api.Assertions.assertThat(enrollment.getEnrolledAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(enrollment.getEnrolledAt()).isAfterOrEqualTo(beforeEnroll);
        Assertions.assertThat(enrollment.getEnrolledAt()).isBeforeOrEqualTo(afterEnroll);
        Assertions.assertThat(enrollment.isActive()).isTrue();
    }


    @Test
    void enrollMentoredGetsExecutedProperly() throws ZerofiltreException {
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        User user = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        user.setEmail("test@gmail.grok");
        user.setPseudoName("tester");
        user = dbUserProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList(), true);
        course = dbCourseProvider.save(course);
        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(user.getId(), course.getId(), true);
        LocalDateTime afterEnroll = LocalDateTime.now();

        assertThat(enrollment).isNotNull();
        Assertions.assertThat(enrollment.getPlan()).isEqualTo(User.Plan.BASIC);
        assertThat(enrollment.getUser().getId()).isEqualTo(user.getId());
        assertThat(enrollment.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(enrollment.getUser().getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollment.getCourse().getEnrolledCount()).isOne();

        assertThat(enrollment.getId()).isNotZero();
        assertThat(enrollment.isCompleted()).isFalse();
        Assertions.assertThat(enrollment.getCompletedLessons()).isEmpty();

        org.assertj.core.api.Assertions.assertThat(enrollment.getEnrolledAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(enrollment.getEnrolledAt()).isAfterOrEqualTo(beforeEnroll);
        Assertions.assertThat(enrollment.getEnrolledAt()).isBeforeOrEqualTo(afterEnroll);
        Assertions.assertThat(enrollment.isActive()).isTrue();
    }



    @Test
    void executeSetSuspendeAt_toNull() throws ZerofiltreException {
        User user = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        user.setEmail("test@gmail.grok");
        user.setPseudoName("tester");
        user = dbUserProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);

        enroll.execute(user.getId(), course.getId(), true);

        Enrollment suspendedEnrollment = suspend.execute(user.getId(), course.getId());
        assertThat(suspendedEnrollment.getSuspendedAt()).isNotNull();

        Enrollment enrollment = enroll.execute(user.getId(), course.getId(), true);

        Assertions.assertThat(enrollment.getSuspendedAt()).isNull();
        Assertions.assertThat(enrollment.getId()).isEqualTo(suspendedEnrollment.getId());

    }
}
