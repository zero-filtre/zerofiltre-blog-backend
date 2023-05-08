package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.doubles.*;

import java.time.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class SuspendTest {
    private Suspend suspend;

    @Test
    void suspendThrowsExceptionWhenUserIsNotEnrolledToCourse() {
        EnrollmentProvider enrollmentProvider = new NotEnrolledEnrollmentProvider();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        suspend = new Suspend(enrollmentProvider, new Found_Published_WithUnknownAuthor_CourseProviderSpy(), chapterProvider);
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> suspend.execute(1, 1))
                .withMessage("You are not enrolled in the course of id 1");
    }

    @Test
    void suspendDeactivatesTheEnrollment() throws ZerofiltreException {
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        LocalDateTime beforeSuspend = LocalDateTime.now();
        ChapterProviderSpy chapterProvider = new ChapterProviderSpy();
        suspend = new Suspend(enrollmentProviderSpy, courseProvider, chapterProvider);
        LocalDateTime afterSuspendPlus10Sec = LocalDateTime.now().plusSeconds(10);

        Enrollment deactivatedEnrollment = suspend.execute(1, 1);
        assertThat(courseProvider.enrollCalledCount).isTrue();
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();

        Assertions.assertThat(deactivatedEnrollment).isNotNull();
        Assertions.assertThat(deactivatedEnrollment.isActive()).isFalse();
        Assertions.assertThat(enrollmentProviderSpy.saveCalled).isTrue();

        org.assertj.core.api.Assertions.assertThat(deactivatedEnrollment.getSuspendedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(deactivatedEnrollment.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        org.assertj.core.api.Assertions.assertThat(deactivatedEnrollment.getSuspendedAt()).isBeforeOrEqualTo(afterSuspendPlus10Sec);

    }
}
