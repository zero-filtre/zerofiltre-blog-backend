package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.doubles.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class SuspendTest {
    private Suspend suspend;

    @Test
    void suspendThrowsExceptionWhenUserIsNotEnrolledToCourse() {
        EnrollmentProvider enrollmentProvider = new NotEnrolledEnrollmentProvider();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        suspend = new Suspend(enrollmentProvider, new Found_Published_WithUnknownAuthor_CourseProviderSpy(), chapterProvider, null);
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
        suspend = new Suspend(enrollmentProviderSpy, courseProvider, chapterProvider, null);
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
    void suspendDelete_Purchase_IfMentored() throws ZerofiltreException {

        EnrollmentProvider enrollmentProvider = new MentoredEnrollmentProviderSpy();

        ChapterProvider chapterProvider = new ChapterProviderSpy();

        CourseProvider courseProvider = mock(CourseProvider.class);

        PurchaseProvider purchaseProvider = mock(PurchaseProvider.class);

        suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider, purchaseProvider);
        suspend.execute(1, 1);

        verify(purchaseProvider, times(1)).delete(1, 45);

    }
}
