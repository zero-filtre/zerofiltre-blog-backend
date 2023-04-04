package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

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
@Import({DBEnrollmentProvider.class, DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBCourseProvider.class})
class SuspendIT {

    Suspend suspend;

    Enroll enroll;

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
        suspend = new Suspend(enrollmentProvider, dbCourseProvider, chapterProvider);
        enroll = new Enroll(enrollmentProvider, dbCourseProvider, dbUserProvider, chapterProvider);
    }

    @Test
    void suspendSavesEnrollmentProperly() throws BlogException, InterruptedException {
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        User user = ZerofiltreUtils.createMockUser(true);
        user.setEmail("test@gmail.grok");
        user.setPseudoName("tester");
        user = dbUserProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);
        enroll.execute(user.getId(), course.getId());
        LocalDateTime beforeSuspend = LocalDateTime.now();
        Enrollment enrollment = suspend.execute(user.getId(), course.getId());
        LocalDateTime afterSuspend = LocalDateTime.now();

        assertThat(enrollment).isNotNull();
        assertThat(enrollment.getUser().getId()).isEqualTo(user.getId());
        assertThat(enrollment.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(enrollment.getUser().getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollment.getCourse().getEnrolledCount()).isZero();

        assertThat(enrollment.getId()).isNotZero();

        org.assertj.core.api.Assertions.assertThat(enrollment.getSuspendedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(enrollment.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        org.assertj.core.api.Assertions.assertThat(enrollment.getEnrolledAt()).isBefore(enrollment.getSuspendedAt());
        org.assertj.core.api.Assertions.assertThat(enrollment.getSuspendedAt()).isBeforeOrEqualTo(afterSuspend);
        Assertions.assertThat(enrollment.isActive()).isFalse();
    }
}
