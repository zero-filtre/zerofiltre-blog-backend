package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.*;

import tech.zerofiltre.blog.util.ZerofiltreUtils;

import tech.zerofiltre.blog.infra.providers.database.purchase.DBPurchaseProvider;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class SuspendTest {
    private Suspend suspend;

    @Test
    void suspendThrowsExceptionWhenUserIsNotEnrolledToCourse() {
        EnrollmentProvider enrollmentProvider = new NotEnrolledEnrollmentProvider();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        suspend = new Suspend(enrollmentProvider, new Found_Published_WithUnknownAuthor_CourseProviderSpy(), chapterProvider, null, null);
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> suspend.execute(1, 1))
                .withMessage("You are not enrolled in the course of id 1");
    }

    @Test
    void suspendDeactivatesTheEnrollment() throws ZerofiltreException {
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        LocalDateTime beforeSuspend = LocalDateTime.now();
        ChapterProviderSpy chapterProvider = new ChapterProviderSpy();
        PurchaseProvider purchaseProvider = mock(PurchaseProvider.class);
        suspend = new Suspend(enrollmentProviderSpy, courseProvider, chapterProvider, purchaseProvider);
        LocalDateTime afterSuspendPlus10Sec = LocalDateTime.now().plusSeconds(10);

        Enrollment deactivatedEnrollment = suspend.execute(1, 1);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();

        Assertions.assertThat(deactivatedEnrollment).isNotNull();
        Assertions.assertThat(deactivatedEnrollment.isActive()).isFalse();
        Assertions.assertThat(enrollmentProviderSpy.saveCalled).isTrue();

        org.assertj.core.api.Assertions.assertThat(deactivatedEnrollment.getSuspendedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(deactivatedEnrollment.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        org.assertj.core.api.Assertions.assertThat(deactivatedEnrollment.getSuspendedAt()).isBeforeOrEqualTo(afterSuspendPlus10Sec);

    }

    @Test
    void suspendDelete_Purchase() throws ZerofiltreException {

        EnrollmentProvider enrollmentProvider = new EnrollmentProviderSpy();

        ChapterProvider chapterProvider = new ChapterProviderSpy();

        CourseProvider courseProvider = mock(CourseProvider.class);

        PurchaseProvider purchaseProvider = mock(PurchaseProvider.class);

        suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider, purchaseProvider, null);
        suspend.execute(1, 1);

        verify(purchaseProvider, times(1)).delete(1, 0);

    }

    @Test
    void suspendDelete_sandbox_if_there_is_any() throws ZerofiltreException  {
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        PurchaseProvider purchaseProvider = mock(PurchaseProvider.class);
        SandboxProvider sandboxProvider = mock(SandboxProvider.class);

        User user = ZerofiltreUtils.createMockUser(false) ;
        Course course = ZerofiltreUtils.createMockCourse(Sandbox.Type.K8S);

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(LocalDateTime.now().minusDays(2));
        enrollment.setLastModifiedAt(enrollment.getEnrolledAt());
        enrollment.setSuspendedAt(LocalDateTime.now().minusDays(1));
        enrollment.setActive(true);
        enrollment.setUser(user);
        enrollment.setCourse(course);

        when(enrollmentProvider.enrollmentOf(user.getId(), course.getId(), true)).thenReturn(Optional.of(enrollment));
        when(enrollmentProvider.save(any())).thenReturn(enrollment);
        suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider, purchaseProvider, sandboxProvider);

        suspend.execute(user.getId(), course.getId());

        verify(sandboxProvider, times(1)).destroy(anyString(), anyString());
    }

    @Test
    void suspendDontDelete_sandbox_if_there_is_not() throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        PurchaseProvider purchaseProvider = mock(PurchaseProvider.class);
        SandboxProvider sandboxProvider = mock(SandboxProvider.class);

        User user = ZerofiltreUtils.createMockUser(false) ;
        Course course = ZerofiltreUtils.createMockCourse(Sandbox.Type.NONE);

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(LocalDateTime.now().minusDays(2));
        enrollment.setLastModifiedAt(enrollment.getEnrolledAt());
        enrollment.setSuspendedAt(LocalDateTime.now().minusDays(1));
        enrollment.setActive(true);
        enrollment.setUser(user);
        enrollment.setCourse(course);

        when(enrollmentProvider.enrollmentOf(user.getId(), course.getId(), true)).thenReturn(Optional.of(enrollment));
        when(enrollmentProvider.save(any())).thenReturn(enrollment);
        suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider, purchaseProvider, sandboxProvider);

        suspend.execute(user.getId(), course.getId());

        verify(sandboxProvider, times(0)).destroy(anyString(), anyString());
    }
}
