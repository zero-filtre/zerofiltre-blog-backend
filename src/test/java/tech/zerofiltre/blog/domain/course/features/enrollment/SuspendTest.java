package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.Page;
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
import tech.zerofiltre.blog.doubles.ChapterProviderSpy;
import tech.zerofiltre.blog.doubles.EnrollmentProviderSpy;
import tech.zerofiltre.blog.doubles.NotEnrolledEnrollmentProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuspendTest {
    private Suspend suspend;

    EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
    ChapterProvider chapterProvider = mock(ChapterProvider.class);
    SandboxProvider sandboxProvider = mock(SandboxProvider.class);
    CourseProvider courseProvider = mock(CourseProvider.class);
    PurchaseProvider purchaseProvider = mock(PurchaseProvider.class);

    @BeforeEach
    void init() throws ZerofiltreException {
        doNothing().when(sandboxProvider).destroy(any(), any());
    }


    @Test
    void suspendThrowsExceptionWhenUserIsNotEnrolledToCourse() {
        EnrollmentProvider enrollmentProvider = new NotEnrolledEnrollmentProvider();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        suspend = new Suspend(enrollmentProvider, chapterProvider, null, sandboxProvider, courseProvider);
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> suspend.execute(1, 1))
                .withMessage("You are not enrolled in the course of id 1");
    }

    @Test
    void suspendDeactivatesTheEnrollment() throws ZerofiltreException {
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        LocalDateTime beforeSuspend = LocalDateTime.now();
        ChapterProviderSpy chapterProvider = new ChapterProviderSpy();
        suspend = new Suspend(enrollmentProviderSpy, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);
        LocalDateTime afterSuspendPlus10Sec = LocalDateTime.now().plusSeconds(10);

        Enrollment deactivatedEnrollment = suspend.execute(1, 1);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();

        assertThat(deactivatedEnrollment).isNotNull();
        assertThat(deactivatedEnrollment.isActive()).isFalse();
        assertThat(enrollmentProviderSpy.saveCalled).isTrue();

        assertThat(deactivatedEnrollment.getSuspendedAt()).isNotNull();
        assertThat(deactivatedEnrollment.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        assertThat(deactivatedEnrollment.getSuspendedAt()).isBeforeOrEqualTo(afterSuspendPlus10Sec);

    }

    @Test
    void suspendDelete_Purchase() throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = new EnrollmentProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();

        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        suspend.execute(1, 1);

        verify(purchaseProvider, times(1)).delete(1, 0);

    }

    @Test
    void suspendDelete_sandbox_if_there_is_any() throws ZerofiltreException {
        User user = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(Sandbox.Type.K8S);

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(LocalDateTime.now().minusDays(2));
        enrollment.setLastModifiedAt(enrollment.getEnrolledAt());
        enrollment.setSuspendedAt(LocalDateTime.now().minusDays(1));
        enrollment.setActive(true);
        enrollment.setUser(user);
        enrollment.setCourse(course);

        when(enrollmentProvider.enrollmentOf(user.getId(), course.getId(), true)).thenReturn(Optional.of(enrollment));
        when(enrollmentProvider.save(any())).thenReturn(enrollment);
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        suspend.execute(user.getId(), course.getId());

        verify(sandboxProvider, times(1)).destroy(anyString(), anyString());
    }

    @Test
    void suspendDontDelete_sandbox_if_there_is_not() throws ZerofiltreException {
        User user = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(Sandbox.Type.NONE);

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(LocalDateTime.now().minusDays(2));
        enrollment.setLastModifiedAt(enrollment.getEnrolledAt());
        enrollment.setSuspendedAt(LocalDateTime.now().minusDays(1));
        enrollment.setActive(true);
        enrollment.setUser(user);
        enrollment.setCourse(course);

        when(enrollmentProvider.enrollmentOf(user.getId(), course.getId(), true)).thenReturn(Optional.of(enrollment));
        when(enrollmentProvider.save(any())).thenReturn(enrollment);
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        suspend.execute(user.getId(), course.getId());

        verify(sandboxProvider, times(0)).destroy(anyString(), anyString());
    }

    @Test
    void suspendDontDeleteAsAdminUser() throws ZerofiltreException {
        //GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(Sandbox.Type.NONE);

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(LocalDateTime.now().minusDays(2));
        enrollment.setLastModifiedAt(enrollment.getEnrolledAt());
        enrollment.setSuspendedAt(LocalDateTime.now().minusDays(1));
        enrollment.setActive(true);
        enrollment.setUser(user);
        enrollment.setCourse(course);

        when(enrollmentProvider.enrollmentOf(user.getId(), course.getId(), true)).thenReturn(Optional.of(enrollment));
        when(enrollmentProvider.save(any())).thenReturn(enrollment);
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        //WHEN
        suspend.execute(user.getId(), course.getId());

        //THEN
        verify(enrollmentProvider).enrollmentOf(anyLong(), anyLong(), anyBoolean());
        verify(enrollmentProvider).save(any(Enrollment.class));
    }

    @Test
    void suspendDontDeleteAsAdminUserForCompanyUser() throws ZerofiltreException {
        //GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(Sandbox.Type.NONE);

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(LocalDateTime.now().minusDays(2));
        enrollment.setLastModifiedAt(enrollment.getEnrolledAt());
        enrollment.setSuspendedAt(LocalDateTime.now().minusDays(1));
        enrollment.setActive(true);
        enrollment.setUser(user);
        enrollment.setCourse(course);

        when(enrollmentProvider.enrollmentOf(user.getId(), course.getId(), true)).thenReturn(Optional.of(enrollment));
        when(enrollmentProvider.save(any())).thenReturn(enrollment);
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        //WHEN
        suspend.execute(user.getId(), course.getId());

        //THEN
        verify(enrollmentProvider).enrollmentOf(anyLong(), anyLong(), anyBoolean());
        verify(enrollmentProvider).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("When I suspend all active enrollments of a user in courses and the user is not part of a company to take these courses, then the enrollments are suspended")
    void suspendAllEnrollments() throws ZerofiltreException {
        //GIVEN
        Course course = new Course();
        course.setId(1);
        course.setEnrolledCount(1);
        course.setSandboxType(Sandbox.Type.NONE);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setId(1);
        enrollment1.setCourse(course);
        enrollment1.setActive(true);
        enrollment1.setForLife(false);
        enrollment1.setCompanyUserId(0);
        enrollment1.setCompanyCourseId(0);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setId(2);
        enrollment2.setCourse(course);
        enrollment2.setActive(true);
        enrollment2.setForLife(false);
        enrollment2.setCompanyUserId(0);
        enrollment2.setCompanyCourseId(0);

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setId(3);
        enrollment3.setCourse(course);
        enrollment3.setActive(true);
        enrollment3.setForLife(true);
        enrollment3.setCompanyUserId(0);
        enrollment3.setCompanyCourseId(0);

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setId(4);
        enrollment4.setCourse(course);
        enrollment4.setActive(false);
        enrollment4.setForLife(false);

        Enrollment enrollment5 = new Enrollment();
        enrollment5.setId(5);
        enrollment5.setCourse(course);
        enrollment5.setActive(true);
        enrollment5.setForLife(false);
        enrollment5.setCompanyUserId(1);
        enrollment5.setCompanyCourseId(0);


        Enrollment enrollment6 = new Enrollment();
        enrollment6.setId(6);
        enrollment6.setCourse(course);
        enrollment6.setActive(true);
        enrollment6.setForLife(false);
        enrollment6.setCompanyUserId(0);
        enrollment6.setCompanyCourseId(1);

        Page<Enrollment> enrollmentsPage = new Page<>();
        List<Enrollment> list = enrollmentsPage.getContent();
        list.add(enrollment1);
        list.add(enrollment2);
        list.add(enrollment3);
        list.add(enrollment4);
        list.add(enrollment5);
        list.add(enrollment6);

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), eq(null), eq(null))).thenReturn(enrollmentsPage);
        when(enrollmentProvider.save(any(Enrollment.class))).thenReturn(enrollment1).thenReturn(enrollment2);

        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        //WHEN
        suspend.all(1, false);

        //THEN
        verify(enrollmentProvider, times(1)).of(anyInt(), anyInt(), anyLong(), eq(null), eq(null));

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(2)).save(captor.capture());
        List<Enrollment> enrolledCaptor = captor.getAllValues();
        assertThat(enrolledCaptor).isNotNull();
        assertThat(enrolledCaptor.get(0).getId()).isEqualTo(enrollment1.getId());
        assertThat(enrolledCaptor.get(0).isActive()).isFalse();
        assertThat(enrolledCaptor.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(enrolledCaptor.get(1).getId()).isEqualTo(enrollment2.getId());
        assertThat(enrolledCaptor.get(1).isActive()).isFalse();
        assertThat(enrolledCaptor.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void suspendAllEnrollmentsNothing() throws ZerofiltreException {
        //GIVEN
        Course course = new Course();
        course.setId(1);
        course.setEnrolledCount(1);
        course.setSandboxType(Sandbox.Type.NONE);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setId(1);
        enrollment1.setCourse(course);
        enrollment1.setActive(false);
        enrollment1.setForLife(true);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setId(2);
        enrollment2.setCourse(course);
        enrollment2.setActive(true);
        enrollment2.setForLife(false);

        Page<Enrollment> enrollmentsPage = new Page<>();
        List<Enrollment> list = enrollmentsPage.getContent();
        list.add(enrollment1);
        list.add(enrollment2);

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), eq(null), eq(null))).thenReturn(enrollmentsPage);

        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        //WHEN
        suspend.all(1, true);

        //THEN
        verify(enrollmentProvider, times(1)).of(anyInt(), anyInt(), anyLong(), eq(null), eq(null));

        verify(enrollmentProvider, never()).save(any(Enrollment.class));
    }

    @Test
    void suspendByLinkId() throws ZerofiltreException {
        //GIVEN
        Course course = new Course();
        course.setId(1);
        course.setEnrolledCount(1);
        course.setSandboxType(Sandbox.Type.NONE);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourse(course);

        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));
        when(enrollmentProvider.save(any(Enrollment.class))).thenReturn(enrollment1).thenReturn(enrollment2);

        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        //WHEN
        suspend.byLinkId(1);

        //THEN
        verify(enrollmentProvider, times(1)).findAll(anyLong(), anyBoolean());

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(2)).save(captor.capture());
        List<Enrollment> enrolledCaptor = captor.getAllValues();
        assertThat(enrolledCaptor).isNotNull();
        assertThat(enrolledCaptor.get(0).isActive()).isFalse();
        assertThat(enrolledCaptor.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(enrolledCaptor.get(1).isActive()).isFalse();
        assertThat(enrolledCaptor.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(courseProvider, times(1)).save(any(Course.class));
    }

    @Test
    void suspendByLinkIdWithSandbox() throws ZerofiltreException {
        //GIVEN
        Course course = new Course();
        course.setId(1);
        course.setEnrolledCount(1);
        course.setSandboxType(Sandbox.Type.K8S);

        User user1 = new User();
        user1.setFullName("user 1");
        user1.setEmail("user1@email.com");

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course);
        enrollment1.setUser(user1);

        User user2 = new User();
        user2.setFullName("user 2");
        user2.setEmail("user2@email.com");

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourse(course);
        enrollment2.setUser(user2);

        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));
        when(enrollmentProvider.save(any(Enrollment.class))).thenReturn(enrollment1).thenReturn(enrollment2);

        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        //WHEN
        suspend.byLinkId(1);

        //THEN
        verify(enrollmentProvider, times(1)).findAll(anyLong(), anyBoolean());

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(2)).save(captor.capture());
        List<Enrollment> enrolledCaptor = captor.getAllValues();
        assertThat(enrolledCaptor).isNotNull();
        assertThat(enrolledCaptor.get(0).isActive()).isFalse();
        assertThat(enrolledCaptor.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(enrolledCaptor.get(1).isActive()).isFalse();
        assertThat(enrolledCaptor.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(sandboxProvider, times(2)).destroy(anyString(), anyString());

        verify(courseProvider, times(1)).save(any(Course.class));
    }

    @Test
    void suspendNothingByLinkId() throws ZerofiltreException {
        //GIVEN
        when(enrollmentProvider.findAll(anyLong(), anyBoolean())).thenReturn(new ArrayList<>());

        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);

        //WHEN
        suspend.byLinkId(1);

        //THEN
        verify(enrollmentProvider, times(1)).findAll(anyLong(), anyBoolean());
        verify(enrollmentProvider, never()).save(any(Enrollment.class));
        verify(sandboxProvider, never()).destroy(anyString(), anyString());
        verify(courseProvider, never()).getEnrolledCount(anyLong());
        verify(courseProvider, never()).save(any(Course.class));
    }

}
