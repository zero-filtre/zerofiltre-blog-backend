package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnrollTest {

    private Enroll enroll;

    @Test
    void executeThrowsResourceNotFoundIfUserNotFound() {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProvider = new FoundCancelledOnlyEnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new NotFoundUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(1, 1))
                .withMessage("We could not find the user with id 1");
    }

    @Test
    void executeThrowsForbiddenActionIfUserNotAdminNorPRO() throws BlogException {
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        Enrollment enrollment = new Enrollment();
        when(enrollmentProvider.save(any())).thenReturn(enrollment);

        User user = ZerofiltreUtils.createMockUser(false);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        enrollment.setCourse(course);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider);
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> enroll.execute(user.getId(), course.getId()));

    }

    @Test
    void executeAsPRO_SetsEnrollmentPlanToPro() throws BlogException {
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User user = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider);
        Enrollment result = enroll.execute(user.getId(), course.getId());
        Assertions.assertThat(result.getPlan()).isEqualTo(User.Plan.PRO);

    }


    @Test
    void executeThrowsResourceNotFoundIfCourseNotFound() {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProviderSpy = new FoundCancelledOnlyEnrollmentProviderSpy();
        CourseProvider courseProvider = new NotFoundCourseProviderSpy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        enroll = new Enroll(enrollmentProviderSpy, courseProvider, userProvider, chapterProvider);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(1, 1))
                .withMessage("We couldn't find the course of id 1 you are trying to enroll to");
    }

    @Test
    void executeThrowsForbiddenActionIfCourseIsNotPublished() {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProviderSpy = new FoundCancelledOnlyEnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        enroll = new Enroll(enrollmentProviderSpy, courseProvider, userProvider, chapterProvider);

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> enroll.execute(1, 1))
                .withMessage("You can not get enrolled into an unpublished course");
    }

    @Test
    void executeSavesEnrollmentProperly() throws BlogException {
        NotFoundEnrollmentProviderDummy enrollmentProviderDummy = new NotFoundEnrollmentProviderDummy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProviderDummy, courseProvider, userProvider, chapterProvider);

        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(1, 1);
        LocalDateTime afterEnroll = LocalDateTime.now();

        Course course = enrollment.getCourse();
        assertThat(course.getEnrolledCount()).isEqualTo(1);
        assertThat(courseProvider.enrollCalledCount).isTrue();

        assertThat(course.getLessonsCount()).isEqualTo(2);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();


        assertThat(courseProvider.enrollCalledCount).isTrue();
        Assertions.assertThat(enrollment).isNotNull();
        Assertions.assertThat(enrollment.getPlan()).isEqualTo(User.Plan.BASIC);
        Assertions.assertThat(enrollment.getId()).isNotZero();
        Assertions.assertThat(enrollment.getCourse().getId()).isNotZero();
        Assertions.assertThat(enrollment.isCompleted()).isFalse();
        Assertions.assertThat(enrollment.getCompletedLessons()).isEmpty();
        Assertions.assertThat(enrollment.getEnrolledAt()).isNotNull();
        Assertions.assertThat(enrollment.getEnrolledAt()).isAfterOrEqualTo(beforeEnroll);
        Assertions.assertThat(enrollment.getEnrolledAt()).isBeforeOrEqualTo(afterEnroll);
        Assertions.assertThat(enrollment.getLastModifiedAt()).isEqualTo(enrollment.getEnrolledAt());
        Assertions.assertThat(enrollment.getLastModifiedAt()).isAfterOrEqualTo(beforeEnroll);
        Assertions.assertThat(enrollment.getLastModifiedAt()).isBeforeOrEqualTo(afterEnroll);
        Assertions.assertThat(enrollment.isActive()).isTrue();
    }

    @Test
    @DisplayName("Enrolling after a suspension should set fields properly")
    void executeSetSuspendeAt_toNull() throws BlogException {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProvider = new FoundCancelledOnlyEnrollmentProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider);

        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(1, 1);
        LocalDateTime afterEnroll = LocalDateTime.now();

        Course course = enrollment.getCourse();
        assertThat(course.getEnrolledCount()).isEqualTo(1);
        assertThat(courseProvider.enrollCalledCount).isTrue();

        assertThat(course.getLessonsCount()).isEqualTo(2);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();

        Assertions.assertThat(enrollment.getSuspendedAt()).isNull();
        Assertions.assertThat(enrollment.isActive()).isTrue();
        Assertions.assertThat(enrollment.getLastModifiedAt()).isAfter(enrollment.getEnrolledAt());
        Assertions.assertThat(enrollment.getLastModifiedAt()).isAfterOrEqualTo(beforeEnroll);
        Assertions.assertThat(enrollment.getLastModifiedAt()).isBeforeOrEqualTo(afterEnroll);
    }

    @Test
    void executeDoesNothingIfUserAlreadyEnrolled() throws BlogException {
        EnrollmentProviderSpy enrollmentProvider = new EnrollmentProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider);

        Enrollment enrollment = enroll.execute(1, 1);

        assertThat(enrollmentProvider.enrollmentOfCalled).isTrue();
        assertThat(enrollmentProvider.saveCalled).isFalse();

        Assertions.assertThat(enrollment.isActive()).isTrue();


    }
}