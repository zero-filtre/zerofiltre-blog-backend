package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollTest {

    private Enroll enroll;
    private SandboxProvider sandboxProvider;
    PurchaseProvider purchaseProvider;

    @BeforeEach
    void setUp() {
        sandboxProvider = mock(SandboxProvider.class);
        purchaseProvider = mock(PurchaseProvider.class);
    }

    @Test
    @DisplayName("A basic user can enroll in a course that is related to a course he is already enrolled into")
    void basicUser_can_enrollInACourse_RelatedToAlreadyEnrolled() throws ZerofiltreException {
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        // Build user's previous enrollment

        Resource resource = Resource.builder()
                .lessonId(13)
                .type("course")
                .url("https://dev.zerofiltre.tech/cours/14")
                .build();
        List<Resource> resources = List.of(resource);

        Lesson lesson = Lesson.builder()
                .resources(resources)
                .build();
        List<Lesson> lessons = List.of(lesson);

        Chapter chapter = Chapter.builder()
                .lessons(lessons)
                .build();
        List<Chapter> chapters = List.of(chapter);
        when(chapterProvider.ofCourseId(10)).thenReturn(chapters);

        Course parentCourse = new Course();
        parentCourse.setStatus(Status.PUBLISHED);
        parentCourse.setMentored(true);
        parentCourse.setId(10);

        // find users enrollments
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(12);
        when(userProvider.userOfId(12)).thenReturn(Optional.of(user));

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(parentCourse);
        Page<Enrollment> enrollments = new Page<>();
        enrollments.setContent(List.of(enrollment));

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), eq(null), eq(null)))
                .thenReturn(enrollments);

        // the related course the user wants to enroll into
        Course relatedCourse = new Course();
        relatedCourse.setStatus(Status.PUBLISHED);
        relatedCourse.setId(14);
        when(courseProvider.courseOfId(14)).thenReturn(Optional.of(relatedCourse));

        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);
        when(enrollmentProvider.enrollmentOf(12, 14, true)).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //act
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);
        Enrollment result = enroll.execute(user.getId(), relatedCourse.getId(), true);

        //assert
        assertThat(result).isNotNull();

    }


    @Test
    void proUser_cannot_enrollInMentored_withoutPurchasing() {
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.empty());

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider);


        User user = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> enroll.execute(1, 1, true));

    }

    @Test
    void proUser_can_enrollInMentored_ifPurchased() throws ZerofiltreException {
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));


        User user = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));


        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider);

        //act
        Enrollment result = enroll.execute(1, 1, true);

        //assert
        assertThat(result).isNotNull();


    }

    @Test
    void basicUser_can_enrollInMentored_ifPurchased() throws ZerofiltreException {
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));


        User user = ZerofiltreUtils.createMockUser(false);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));


        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider);

        //act
        Enrollment result = enroll.execute(1, 1, false);

        //assert
        assertThat(result).isNotNull();


    }

    @Test
    void adminUser_can_enrollInMentored_withoutPurchasing() throws ZerofiltreException {
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider);

        Assertions.assertThatNoException()
                .isThrownBy(() -> enroll.execute(1, 1, true));

    }

    @Test
    void throwsResourceNotFoundIfUserNotFound() {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProvider = new FoundCancelledOnlyEnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new NotFoundUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(1, 1, true))
                .withMessage("We could not find the user with id 1");
    }

    @Test
    void throwsForbiddenAction_IfUser_NotAdminNorPRO() {
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);


        User user = ZerofiltreUtils.createMockUser(false);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(new Course()));

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), eq(null), eq(null)))
                .thenReturn(new Page<>());


        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> enroll.execute(user.getId(), 5, true));

    }

    @Test
    void enrollAsPRO_SetsEnrollmentPlanToPro() throws ZerofiltreException {
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

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);
        Enrollment result = enroll.execute(user.getId(), course.getId(), true);
        Assertions.assertThat(result.getPlan()).isEqualTo(User.Plan.PRO);

    }

    @Test
    void enrollMentoredAsPRO_DoesNot_SetsEnrollmentPlanToPro() throws ZerofiltreException {
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
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);
        Enrollment result = enroll.execute(user.getId(), course.getId(), true);
        Assertions.assertThat(result.getPlan()).isEqualTo(User.Plan.BASIC);

    }

    @Test
    void enrollAsNonPRO_SetsEnrollmentPlanToBasic_ifNotFromEndUser() throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(856);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);
        Enrollment result = enroll.execute(user.getId(), course.getId(), false);
        Assertions.assertThat(result.getPlan()).isEqualTo(User.Plan.BASIC);
        Assertions.assertThat(result.getUser().getId()).isEqualTo(856);


    }


    @Test
    void throwsResourceNotFound_IfCourseNotFound() {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProviderSpy = new FoundCancelledOnlyEnrollmentProviderSpy();
        CourseProvider courseProvider = new NotFoundCourseProviderSpy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        enroll = new Enroll(enrollmentProviderSpy, courseProvider, userProvider, chapterProvider, null, null);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(1, 1, true))
                .withMessage("We couldn't find the course of id 1 you are trying to enroll to");
    }

    @Test
    void throws_ForbiddenAction_IfCourseIsNotPublished() {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProviderSpy = new FoundCancelledOnlyEnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = new ChapterProviderSpy();
        enroll = new Enroll(enrollmentProviderSpy, courseProvider, userProvider, chapterProvider, null, null);

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> enroll.execute(1, 1, true))
                .withMessage("You can not get enrolled into an unpublished course");
    }

    @Test
    @DisplayName("A legitimate user(admin,pro,buyer) must be able to enroll a non mentored course properly")
    void saves_EnrollmentProperly_ifCourseNotMentored_andNoPurchases() throws ZerofiltreException {
        NotFoundEnrollmentProviderDummy enrollmentProviderDummy = new NotFoundEnrollmentProviderDummy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProviderDummy, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider);

        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(1, 1, true);
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
    void setsSuspendedAt_toNull() throws ZerofiltreException {
        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProvider = new FoundCancelledOnlyEnrollmentProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);

        LocalDateTime beforeEnroll = LocalDateTime.now();
        Enrollment enrollment = enroll.execute(1, 1, true);
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
    void doesNothingIfUserAlreadyEnrolled() throws ZerofiltreException {
        EnrollmentProviderSpy enrollmentProvider = new EnrollmentProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null);

        Enrollment enrollment = enroll.execute(1, 1, true);

        assertThat(enrollmentProvider.enrollmentOfCalled).isTrue();
        assertThat(enrollmentProvider.saveCalled).isFalse();

        Assertions.assertThat(enrollment.isActive()).isTrue();


    }

    @Test
    void doesNot_callSandboxProvider_ifCourseDoesNotNeedSandbox() throws ZerofiltreException {
        NotFoundEnrollmentProviderDummy enrollmentProviderDummy = new NotFoundEnrollmentProviderDummy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();

        sandboxProvider = mock(SandboxProvider.class);

        enroll = new Enroll(enrollmentProviderDummy, courseProvider, userProvider, chapterProvider, sandboxProvider, null);


        //act
        enroll.execute(1, 1, true);


        //assert
        verify(sandboxProvider, times(0)).initialize(any(), anyString());

    }

    @Test
    void callsSandboxProvider_ifCourseNeedsSandbox() throws ZerofiltreException {
        NotFoundEnrollmentProviderDummy enrollmentProviderDummy = new NotFoundEnrollmentProviderDummy();
        CourseProvider courseProvider = mock(CourseProvider.class);

        User author = ZerofiltreUtils.createMockUser(false);
        Course mockCourse = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), new ArrayList<>());
        mockCourse.setSandboxType(Sandbox.Type.K8S);
        when(courseProvider.courseOfId(anyLong())).thenReturn(
                Optional.of(mockCourse)
        );

        UserProvider userProvider = new FoundNonAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();

        sandboxProvider = mock(SandboxProvider.class);
        when(sandboxProvider.initialize(any(), anyString())).thenReturn(new Sandbox());

        enroll = new Enroll(enrollmentProviderDummy, courseProvider, userProvider, chapterProvider, sandboxProvider, null);


        //act
        enroll.execute(1, 1, false);
        ZerofiltreUtils.sleep(500);


        //assert
        verify(sandboxProvider, times(1)).initialize(any(), anyString());

    }


}