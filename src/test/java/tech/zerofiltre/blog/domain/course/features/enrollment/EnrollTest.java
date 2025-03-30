package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.company.features.CompanyService;
import tech.zerofiltre.blog.domain.company.features.CompanyUserService;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
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
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollTest {

    private Enroll enroll;
    private SandboxProvider sandboxProvider;
    private PurchaseProvider purchaseProvider;
    private CompanyCourseProvider companyCourseProvider;
    private CompanyUserProvider companyUserProvider;
    private CompanyProvider companyProvider;

    @Mock
    private CompanyService companyService;

    @Mock
    private CompanyCourseService companyCourseService;

    @Mock
    private CompanyUserService companyUserService;

    @BeforeEach
    void setUp() {
        sandboxProvider = mock(SandboxProvider.class);
        purchaseProvider = mock(PurchaseProvider.class);
        companyProvider = mock(CompanyProvider.class);
        companyCourseProvider = mock(CompanyCourseProvider.class);
        companyUserProvider = mock(CompanyUserProvider.class);
    }

    @Test
    @DisplayName("A basic user can enroll in a course that is related to a course he is already enrolled into")
    void basicUser_can_enrollInACourse_RelatedToAlreadyEnrolled() throws ZerofiltreException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        // Build user's previous enrollment
        Course parentCourse = new Course();
        parentCourse.setStatus(Status.PUBLISHED);
        parentCourse.setMentored(true);
        parentCourse.setId(20);

        // find users enrollments
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(12);
        when(userProvider.userOfId(12)).thenReturn(Optional.of(user));

        // the related course the user wants to enroll into
        Course relatedCourse = new Course();
        relatedCourse.setStatus(Status.PUBLISHED);
        relatedCourse.setId(24);
        when(courseProvider.courseOfId(24)).thenReturn(Optional.of(relatedCourse));

        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);
        when(enrollmentProvider.enrollmentOf(12, 24, true)).thenReturn(Optional.empty());

        Chapter chapter = new Chapter.ChapterBuilder()
                .id(30)
                .courseId(parentCourse.getId())
                .build();

        Lesson lesson = new Lesson.LessonBuilder()
                .id(40)
                .chapterId(chapter.getId())
                .build();
        chapter.setLessons(Collections.singletonList(lesson));

        Resource resource = new Resource.ResourceBuilder()
                .id(54)
                .type("course")
                .url("https://path/cours/24-name-of-course")
                .lessonId(lesson.getId())
                .build();
        lesson.setResources(Collections.singletonList(resource));

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setUser(user);
        enrollment.setCourse(relatedCourse);
        enrollment.setActive(true);
        List<Enrollment> enrollmentList = Collections.singletonList(enrollment);
        Page<Enrollment> enrollmentPage = new Page<>();
        enrollmentPage.setContent(enrollmentList);
        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any()))
                .thenReturn(enrollmentPage);

        when(chapterProvider.ofCourseId(anyLong()))
                .thenReturn(Collections.singletonList(chapter));

        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //act
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);
        Enrollment result = enroll.execute(user.getId(), relatedCourse.getId(), 0, false);

        //assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getCourse()).isEqualTo(relatedCourse);
        assertThat(result.isForLife()).isTrue();

        verify(purchaseProvider, times(0)).purchaseOf(anyLong(), anyLong());

    }

    @Test
    void proUser_cannot_enrollInMentored_withoutPurchasing() {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, null, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);


        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any())).thenReturn(new Page<>());
        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.empty());

        //assert
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> enroll.execute(1, 1, 0, false));
    }

    @Test
    void proUser_can_enrollInMentored_ifPurchased() throws ZerofiltreException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));


        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));


        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any())).thenReturn(new Page<>());
        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(1, 1, 0, false);

        //assert
        assertThat(result).isNotNull();

    }

    @Test
    void basicUser_can_enrollInMentored_ifPurchased() throws ZerofiltreException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        User user = ZerofiltreUtilsTest.createMockUser(false);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any())).thenReturn(new Page<>());
        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(1, 1, 0, false);

        //assert
        assertThat(result).isNotNull();

    }

    @Test
    void adminUser_can_enrollInMentored_withoutPurchasing() throws ZerofiltreException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(1, 1, 0, false);

        //assert
        Assertions.assertThatNoException()
                .isThrownBy(() -> enroll.execute(1, 1, 0, false));
        assertThat(result.isForLife()).isTrue();
        verify(purchaseProvider, times(0)).purchaseOf(anyLong(), anyLong());

    }

    @Test
    void throwsResourceNotFoundIfUserNotFound() {
        //arrange
        UserProvider userProvider = new NotFoundUserProviderSpy();

        //act
        enroll = new Enroll(null, null, userProvider, null, null, null, companyProvider, companyCourseProvider, companyUserProvider, null);

        //assert
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(1, 1, 0, false))
                .withMessage("We could not find the user with id 1");
    }

    @Test
    void enrollAsUserPRO_SetsEnrollmentForLifeAsFalse() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(user.getId(), course.getId(), 0, false);

        //assert
        assertThat(result.isForLife()).isFalse();
        verify(purchaseProvider, times(0)).purchaseOf(anyLong(), anyLong());
    }

    @Test
    void enrollAsUserBASIC_SetsEnrollmentForLifeAsTrue() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPlan(User.Plan.BASIC);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any())).thenReturn(new Page<>());
        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(user.getId(), course.getId(), 0, false);

        //assert
        assertThat(result.isForLife()).isTrue();

    }

    @Test
    void enrollAsUserADMIN_SetsEnrollmentForLifeTrue() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(1, course.getId(), 0, false);

        //assert
        assertThat(result.isForLife()).isTrue();

    }

    @Test
    void enrollMentoredAsUserBASIC_SetsEnrollmentForLifeAsTrue() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPlan(User.Plan.BASIC);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any())).thenReturn(new Page<>());
        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(user.getId(), course.getId(), 0, false);

        //assert
        assertThat(result.isForLife()).isTrue();

    }

    @Test
    void enrollMentoredAsUserADMIN_SetsEnrollmentForLifeTrue() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(1, course.getId(), 0, false);

        //assert
        assertThat(result.isForLife()).isTrue();

    }

    @Test
    void enrollMentoredAsPRO_Does_SetsEnrollmentForLifeTrue() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());
        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any())).thenReturn(new Page<>());
        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment result = enroll.execute(user.getId(), course.getId(), 0, false);

        //assert
        assertThat(result.isForLife()).isTrue();

    }

    @Test
    void throwsResourceNotFound_IfCourseNotFound() {
        //arrange
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        CourseProvider courseProvider = new NotFoundCourseProviderSpy();

        enroll = new Enroll(null, courseProvider, userProvider, null, null, null, companyProvider, companyCourseProvider, companyUserProvider, null);

        //assert
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(1, 1,0, false))
                .withMessage("We couldn't find the course of id 1 you are trying to enroll to");

    }

    @Test
    void throws_ForbiddenAction_IfCourseIsNotPublished() {
        //arrange
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        CourseProvider courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        enroll = new Enroll(null, courseProvider, userProvider, null, null, null, companyProvider, companyCourseProvider, companyUserProvider, null);

        //assert
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> enroll.execute(1, 1,0, false))
                .withMessage("You can not get enrolled into an unpublished course");
    }

    @Test
    @DisplayName("A legitimate user(admin,buyer) must be able to enroll a non mentored course properly")
    void saves_EnrollmentProperly_ifCourseNotMentored_andNoPurchases() throws ZerofiltreException {
        //arrange
        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        NotFoundEnrollmentProviderDummy enrollmentProviderDummy = new NotFoundEnrollmentProviderDummy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProviderDummy, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        LocalDateTime beforeEnroll = LocalDateTime.now();

        //act
        Enrollment enrollment = enroll.execute(1, 1, 0, false);
        LocalDateTime afterEnroll = LocalDateTime.now();

        Course course = enrollment.getCourse();

        //assert
        assertThat(course.getEnrolledCount()).isEqualTo(1);
        assertThat(courseProvider.enrollCalledCount).isTrue();

        assertThat(course.getLessonsCount()).isEqualTo(2);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();


        assertThat(courseProvider.enrollCalledCount).isTrue();
        assertThat(enrollment).isNotNull();
        assertThat(enrollment.isForLife()).isEqualTo(true);
        assertThat(enrollment.getId()).isNotZero();
        assertThat(enrollment.getCourse().getId()).isNotZero();
        assertThat(enrollment.isCompleted()).isFalse();
        Assertions.assertThat(enrollment.getCompletedLessons()).isEmpty();
        assertThat(enrollment.getEnrolledAt()).isNotNull();
        assertThat(enrollment.getEnrolledAt()).isAfterOrEqualTo(beforeEnroll);
        assertThat(enrollment.getEnrolledAt()).isBeforeOrEqualTo(afterEnroll);
        assertThat(enrollment.getLastModifiedAt()).isEqualTo(enrollment.getEnrolledAt());
        assertThat(enrollment.getLastModifiedAt()).isAfterOrEqualTo(beforeEnroll);
        assertThat(enrollment.getLastModifiedAt()).isBeforeOrEqualTo(afterEnroll);
        assertThat(enrollment.isActive()).isTrue();
    }

    @Test
    @DisplayName("Enrolling after a suspension should set fields properly")
    void setsSuspendedAt_toNull() throws ZerofiltreException {
        //arrange
        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        FoundCancelledOnlyEnrollmentProviderSpy enrollmentProvider = new FoundCancelledOnlyEnrollmentProviderSpy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, null, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        LocalDateTime beforeEnroll = LocalDateTime.now();

        //act
        Enrollment enrollment = enroll.execute(1, 1, 0, false);
        LocalDateTime afterEnroll = LocalDateTime.now();

        Course course = enrollment.getCourse();

        //assert
        assertThat(course.getEnrolledCount()).isEqualTo(1);
        assertThat(courseProvider.enrollCalledCount).isTrue();

        assertThat(course.getLessonsCount()).isEqualTo(2);
        assertThat(chapterProvider.ofCourseIdCalled).isTrue();

        assertThat(enrollment.getSuspendedAt()).isNull();
        assertThat(enrollment.isActive()).isTrue();
        assertThat(enrollment.getLastModifiedAt()).isAfter(enrollment.getEnrolledAt());
        assertThat(enrollment.getLastModifiedAt()).isAfterOrEqualTo(beforeEnroll);
        assertThat(enrollment.getLastModifiedAt()).isBeforeOrEqualTo(afterEnroll);
    }

    @Test
    void doesNothingIfUserAlreadyEnrolled() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), eq(true))).thenReturn(Optional.of(new Enrollment()));

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, null, null, null, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        Enrollment enrollment = enroll.execute(1, 1, 0, false);

        //assert
        assertThat(enrollment.isActive()).isTrue();

        verify(enrollmentProvider, times(1)).enrollmentOf(anyLong(), anyLong(), eq(true));
    }

    @Test
    @Disabled("Not stable, to be fixed")
    void doesNot_callSandboxProvider_ifCourseDoesNotNeedSandbox() throws ZerofiltreException {
        //arrange
        NotFoundEnrollmentProviderDummy enrollmentProviderDummy = new NotFoundEnrollmentProviderDummy();
        Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        UserProvider userProvider = new FoundAdminUserProviderSpy();
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();

        sandboxProvider = mock(SandboxProvider.class);

        enroll = new Enroll(enrollmentProviderDummy, courseProvider, userProvider, chapterProvider, sandboxProvider, null, companyProvider, companyCourseProvider, companyUserProvider, null);

        //act
        enroll.execute(1, 1, 0, false);

        //assert
        verify(sandboxProvider, times(0)).initialize(any(), anyString());

    }

    @Test
    void callsSandboxProvider_ifCourseNeedsSandbox() throws ZerofiltreException {
        //arrange
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = new FoundNonAdminUserProviderSpy();
        CourseProvider courseProvider = mock(CourseProvider.class);
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), new ArrayList<>());
        mockCourse.setSandboxType(Sandbox.Type.K8S);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(mockCourse);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty()).thenReturn(Optional.of(enrollment));
        when(enrollmentProvider.of(anyInt(), anyInt(), anyLong(), any(), any())).thenReturn(new Page<>());
        when(purchaseProvider.purchaseOf(anyLong(), anyLong())).thenReturn(Optional.of(new Purchase()));

        when(enrollmentProvider.save(any())).thenReturn(enrollment);

        sandboxProvider = mock(SandboxProvider.class);
        when(sandboxProvider.initialize(any(), anyString())).thenReturn(new Sandbox());

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        //act
        enroll.execute(1, 1, 0, false);
        ZerofiltreUtils.sleep(500);

        //assert
        verify(sandboxProvider, times(1)).initialize(any(), anyString());

    }

    @Test
    @DisplayName("When a company user enrolls in a company course, the enrollment is created")
    void shouldCreateEnrollment_whenEnrollCompanyUserToCompanyCourse() throws ZerofiltreException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        long companyId = 9L;
        // Build user's previous enrollment
        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        course.setId(20);

        // find users enrollments
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(12);
        when(userProvider.userOfId(12)).thenReturn(Optional.of(user));

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        Chapter chapter = new Chapter.ChapterBuilder()
                .id(30)
                .courseId(course.getId())
                .build();

        Lesson lesson = new Lesson.LessonBuilder()
                .id(40)
                .chapterId(chapter.getId())
                .build();
        chapter.setLessons(Collections.singletonList(lesson));

        Resource resource = new Resource.ResourceBuilder()
                .id(54)
                .type("course")
                .url("https://path/cours/24-name-of-course")
                .lessonId(lesson.getId())
                .build();
        lesson.setResources(Collections.singletonList(resource));

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setActive(true);
        List<Enrollment> enrollmentList = Collections.singletonList(enrollment);
        Page<Enrollment> enrollmentPage = new Page<>();
        enrollmentPage.setContent(enrollmentList);

        when(chapterProvider.ofCourseId(anyLong()))
                .thenReturn(Collections.singletonList(chapter));

        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(12, companyId, course.getId(), true, LocalDateTime.now(), null);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(23, companyId, user.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyUser));

        //act
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        Enrollment result = enroll.execute(user.getId(), course.getId(), companyId, false);

        //assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getCourse()).isEqualTo(course);
        assertThat(result.isForLife()).isFalse();
        assertThat(result.getCompanyCourseId()).isEqualTo(linkCompanyCourse.getId());
        assertThat(result.getCompanyUserId()).isEqualTo(linkCompanyUser.getId());
    }

    @Test
    @DisplayName("When I search a link between a company and a course does not exist, then an exception is thrown")
    void shouldNothing_whenSearchLinkBetweenCompanyAndCourse() throws ResourceNotFoundException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(12);
        when(userProvider.userOfId(12)).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        course.setId(20);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(anyLong(), anyLong())).thenThrow(ResourceNotFoundException.class);

        //act and assert
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyCourseService", companyCourseService);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(user.getId(), course.getId(), 1L, false));
    }

    @Test
    @DisplayName("When I search a link between a company and a user does not exist, then an exception is thrown")
    void shouldNothing_whenSearchLinkBetweenCompanyAndUser() throws ResourceNotFoundException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(12);
        when(userProvider.userOfId(12)).thenReturn(Optional.of(user));

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        course.setId(20);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        when(companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(anyLong(), anyLong())).thenReturn(12L);
        when(companyUserService.getLinkCompanyUserIdIfUserIsActive(anyLong(), anyLong())).thenThrow(ResourceNotFoundException.class);

        //act and assert
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyCourseService", companyCourseService);
        ReflectionTestUtils.setField(enroll, "companyUserService", companyUserService);

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> enroll.execute(user.getId(), course.getId(), 1L, false));
    }

    @Test
    @DisplayName("When a company user enrolls in a company course without giving the company id, he is enrolled")
    void shouldCreateEnrollment_whenEnrollCompanyUserToCompanyCourseWithoutCompanyId() throws ZerofiltreException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(enrollmentProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        long companyId = 9L;
        // Build user's previous enrollment
        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setMentored(true);
        course.setId(20);

        // find users enrollments
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(12);
        when(userProvider.userOfId(12)).thenReturn(Optional.of(user));

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(0);

        Chapter chapter = new Chapter.ChapterBuilder()
                .id(30)
                .courseId(course.getId())
                .build();

        Lesson lesson = new Lesson.LessonBuilder()
                .id(40)
                .chapterId(chapter.getId())
                .build();
        chapter.setLessons(Collections.singletonList(lesson));

        Resource resource = new Resource.ResourceBuilder()
                .id(54)
                .type("course")
                .url("https://path/cours/24-name-of-course")
                .lessonId(lesson.getId())
                .build();
        lesson.setResources(Collections.singletonList(resource));

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setActive(true);
        List<Enrollment> enrollmentList = Collections.singletonList(enrollment);
        Page<Enrollment> enrollmentPage = new Page<>();
        enrollmentPage.setContent(enrollmentList);

        when(chapterProvider.ofCourseId(anyLong()))
                .thenReturn(Collections.singletonList(chapter));

        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(12, companyId, course.getId(), true, LocalDateTime.now(), null);
        when(companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(anyLong(), anyLong())).thenReturn(linkCompanyCourse.getId());

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(23, companyId, user.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null);
        when(companyUserService.getLinkCompanyUserIdIfUserIsActive(anyLong(), anyLong())).thenReturn(linkCompanyUser.getId());

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(List.of(companyId));

        //act
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);
        ReflectionTestUtils.setField(enroll, "companyCourseService", companyCourseService);
        ReflectionTestUtils.setField(enroll, "companyUserService", companyUserService);

        Enrollment result = enroll.execute(user.getId(), course.getId(), 0, false);

        //assert
        verify(companyService).findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseService).getLinkCompanyCourseIdIfCourseIsActive(anyLong(), anyLong());
        verify(companyUserService).getLinkCompanyUserIdIfUserIsActive(anyLong(), anyLong());

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getCourse()).isEqualTo(course);
        assertThat(result.isForLife()).isFalse();
        assertThat(result.getCompanyCourseId()).isEqualTo(linkCompanyCourse.getId());
        assertThat(result.getCompanyUserId()).isEqualTo(linkCompanyUser.getId());
    }

    @Test
    @DisplayName("If the user who is neither a pro nor an admin has not purchased the course and a course from an admin account, then they are completely enrolled")
    void givenUserAndCourseAndCompanyAndFromAdminIsTrue_whenExecute_thenEnroll() throws ZerofiltreException {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setId(20);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(12);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setActive(true);

        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());
        when(enrollmentProvider.save(any())).thenReturn(enrollment);
        when(courseProvider.getEnrolledCount(anyLong())).thenReturn(1);
        when(chapterProvider.ofCourseId(anyLong())).thenReturn(new ArrayList<>());

        //act
        Enrollment result = enroll.execute(user.getId(), course.getId(), 0, true);

        //assert
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getCourse()).isEqualTo(course);
    }

    @Test
    @DisplayName("If the user who is neither a pro nor an admin has not purchased the course and a course from a non-admin account, then an exception is thrown")
    void givenUserAndCourseAndFromAdminIsFalse_whenExecute_thenThrowException() {
        //arrange
        CourseProvider courseProvider = mock(CourseProvider.class);
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        ChapterProvider chapterProvider = mock(ChapterProvider.class);

        when(companyService.findAllCompanyIdByUserIdAndCourseId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, null, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, null);
        ReflectionTestUtils.setField(enroll, "companyService", companyService);

        Course course = new Course();
        course.setStatus(Status.PUBLISHED);
        course.setId(20);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(12);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setActive(true);

        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //act
        assertThrows(Exception.class, () -> enroll.execute(user.getId(), course.getId(), 0, false));
    }

}