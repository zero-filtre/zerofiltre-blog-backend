package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyProvider;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyUserProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.purchase.DBPurchaseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBEnrollmentProvider.class, DBChapterProvider.class, DBPurchaseProvider.class, DBCompanyCourseProvider.class, DBCompanyProvider.class, DBCompanyUserProvider.class})
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

    @Autowired
    DBPurchaseProvider dbPurchaseProvider;

    @Autowired
    DBCompanyProvider dbCompanyProvider;

    @Autowired
    DBCompanyUserProvider dbCompanyUserProvider;

    @Autowired
    DBCompanyCourseProvider dbCompanyCourseProvider;

    @Mock
    private SandboxProvider sandboxProvider;

    @Mock
    private DataChecker checker;

    @Mock
    private CompanyCourseService companyCourseService;

    @BeforeEach
    void init() throws ZerofiltreException {
        doNothing().when(sandboxProvider).destroy(any(), any());
        enroll = new Enroll(enrollmentProvider, dbCourseProvider, dbUserProvider, chapterProvider, null, dbPurchaseProvider, checker, companyCourseService);
        suspend = new Suspend(enrollmentProvider, chapterProvider, dbPurchaseProvider, sandboxProvider, dbCourseProvider);
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
        Enrollment enrollment = enroll.execute(user.getId(), course.getId(), 0);
        LocalDateTime afterEnroll = LocalDateTime.now();

        assertThat(enrollment).isNotNull();
        assertThat(enrollment.isForLife()).isEqualTo(false);
        assertThat(enrollment.getUser().getId()).isEqualTo(user.getId());
        assertThat(enrollment.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(enrollment.getUser().getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollment.getCourse().getEnrolledCount()).isOne();

        assertThat(enrollment.getId()).isNotZero();
        assertThat(enrollment.isCompleted()).isFalse();
        Assertions.assertThat(enrollment.getCompletedLessons()).isEmpty();

        assertThat(enrollment.getEnrolledAt()).isNotNull();
        assertThat(enrollment.getEnrolledAt()).isAfterOrEqualTo(beforeEnroll);
        assertThat(enrollment.getEnrolledAt()).isBeforeOrEqualTo(afterEnroll);
        assertThat(enrollment.isActive()).isTrue();
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

        Purchase purchase = ZerofiltreUtils.createMockPurchase(1, user, course, LocalDateTime.now());
        dbPurchaseProvider.save(purchase);

        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(user.getId(), course.getId(), 0);
        LocalDateTime afterEnroll = LocalDateTime.now();

        assertThat(enrollment).isNotNull();
        assertThat(enrollment.isForLife()).isEqualTo(true);
        assertThat(enrollment.getUser().getId()).isEqualTo(user.getId());
        assertThat(enrollment.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(enrollment.getUser().getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollment.getCourse().getEnrolledCount()).isOne();

        assertThat(enrollment.getId()).isNotZero();
        assertThat(enrollment.isCompleted()).isFalse();
        Assertions.assertThat(enrollment.getCompletedLessons()).isEmpty();

        assertThat(enrollment.getEnrolledAt()).isNotNull();
        assertThat(enrollment.getEnrolledAt()).isAfterOrEqualTo(beforeEnroll);
        assertThat(enrollment.getEnrolledAt()).isBeforeOrEqualTo(afterEnroll);
        assertThat(enrollment.isActive()).isTrue();
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

        enroll.execute(user.getId(), course.getId(), 0);

        Enrollment suspendedEnrollment = suspend.execute(user.getId(), course.getId());
        assertThat(suspendedEnrollment.getSuspendedAt()).isNotNull();

        Enrollment enrollment = enroll.execute(user.getId(), course.getId(), 0);

        assertThat(enrollment.getSuspendedAt()).isNull();
        assertThat(enrollment.getId()).isEqualTo(suspendedEnrollment.getId());

    }

    @Test
    void givenCompanyUserAndCompanyCourse_enrollGetsExecutedProperly() throws ZerofiltreException {
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        User user = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        user.setEmail("test@gmail.grok");
        user.setPseudoName("tester");
        user = dbUserProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);

        Company company = dbCompanyProvider.save(new Company(0, "company 1", "000000001"));
        assertThat(company).isNotNull();

        LinkCompanyUser linkCompanyUser = dbCompanyUserProvider.save(new LinkCompanyUser(company.getId(), user.getId(), LinkCompanyUser.Role.ADMIN));
        assertThat(linkCompanyUser).isNotNull();

        LinkCompanyCourse linkCompanyCourse = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course.getId(), true, LocalDateTime.now(), null));
        assertThat(linkCompanyCourse).isNotNull();
        assertThat(linkCompanyCourse.getId()).isEqualTo(linkCompanyCourse.getId());

        when(companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(anyLong(), anyLong())).thenReturn(linkCompanyCourse.getId());

        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(user.getId(), course.getId(), company.getId());
        LocalDateTime afterEnroll = LocalDateTime.now();

        assertThat(enrollment).isNotNull();
        assertThat(enrollment.isForLife()).isEqualTo(false);
        assertThat(enrollment.getUser().getId()).isEqualTo(user.getId());
        assertThat(enrollment.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(enrollment.getUser().getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollment.getCourse().getEnrolledCount()).isOne();

        assertThat(enrollment.getId()).isNotZero();
        assertThat(enrollment.isCompleted()).isFalse();
        Assertions.assertThat(enrollment.getCompletedLessons()).isEmpty();

        assertThat(enrollment.getEnrolledAt()).isNotNull();
        assertThat(enrollment.getEnrolledAt()).isAfterOrEqualTo(beforeEnroll);
        assertThat(enrollment.getEnrolledAt()).isBeforeOrEqualTo(afterEnroll);
        assertThat(enrollment.isActive()).isTrue();

        assertThat(enrollment.getCompanyCourseId()).isEqualTo(linkCompanyCourse.getId());
    }

}
