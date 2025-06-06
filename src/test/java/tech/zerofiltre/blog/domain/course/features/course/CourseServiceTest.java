package tech.zerofiltre.blog.domain.course.features.course;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;
import static tech.zerofiltre.blog.util.ZerofiltreUtilsTest.TEST_COURSE_TITLE;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    public static final String THIS_IS_MY_TITLE = "This is my title";

    private CourseProvider courseProvider;
    private TagProvider tagProvider;
    private LoggerProvider loggerProvider;
    private Course course;
    private User editor;

    @Mock
    DataChecker checker;

    @Mock
    CompanyCourseProvider companyCourseProvider;

    @Mock
    EnrollmentProvider enrollmentProvider;

    @Mock
    CompanyCourseService companyCourseService;

    @BeforeEach
    void init() {
        course = new Course();
        editor = ZerofiltreUtilsTest.createMockUser(false);
    }


    @Test
    @DisplayName("When a user initializes a course, the course is initialized.")
    void shouldInitCourse_whenUserInitializesCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(1);
        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course course1 = courseService.init(THIS_IS_MY_TITLE, user, 0);

        //THEN
        assertThat(course1.getTitle()).isEqualTo(THIS_IS_MY_TITLE);
        assertThat(course1.getStatus()).isEqualTo(DRAFT);
        LocalDateTime createdAt = course1.getCreatedAt();
        assertThat(createdAt).isAfterOrEqualTo(beforeSave);
        assertThat(course1.getLastSavedAt()).isEqualTo(createdAt);
        assertThat(course1.getLastPublishedAt()).isNull();
        assertThat(course1.getPublishedAt()).isNull();
        assertThat(course1.getAuthor().getId()).isEqualTo(user.getId());
        assertThat(course1.getAuthor().getEmail()).isEqualTo(user.getEmail());
        assertThat(((NotFoundCourseProviderSpy) courseProvider).registerCourseCalled).isTrue();
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a platform admin initializes a company course, the course is initialized and a link between company and course is created.")
    void shouldInitCourseAndCreateLink_whenPlatformAdminInitializesCompanyCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        long companyId = 1;
        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        user.setId(1);

        doNothing().when(checker).checkCompanyExistence(anyLong());
        doNothing().when(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course course1 = courseService.init(THIS_IS_MY_TITLE, user, companyId);

        //THEN
        assertThat(course1.getTitle()).isEqualTo(THIS_IS_MY_TITLE);
        assertThat(course1.getStatus()).isEqualTo(DRAFT);
        LocalDateTime createdAt = course1.getCreatedAt();
        assertThat(createdAt).isAfterOrEqualTo(beforeSave);
        assertThat(course1.getLastSavedAt()).isEqualTo(createdAt);
        assertThat(course1.getLastPublishedAt()).isNull();
        assertThat(course1.getPublishedAt()).isNull();
        assertThat(course1.getAuthor().getId()).isEqualTo(user.getId());
        assertThat(course1.getAuthor().getEmail()).isEqualTo(user.getEmail());
        assertThat(((NotFoundCourseProviderSpy) courseProvider).registerCourseCalled).isTrue();
        verify(checker).checkCompanyExistence(anyLong());
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        Assertions.assertThat(linkCompanyCourseCaptured.getCompanyId()).isEqualTo(companyId);
        Assertions.assertThat(linkCompanyCourseCaptured.getCourseId()).isEqualTo(course1.getId());
        Assertions.assertThat(linkCompanyCourseCaptured.isExclusive()).isTrue();
        Assertions.assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        Assertions.assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        Assertions.assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When a company admin or company editor initializes a company course, the course is initialized and a link between company and course is created.")
    void shouldInitCourseAndCreateLink_whenCompanyAdminORCompanyEditorInitializesCompanyCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        long companyId = 1;
        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(1);

        doNothing().when(checker).checkCompanyExistence(anyLong());
        doNothing().when(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course course1 = courseService.init(THIS_IS_MY_TITLE, user, companyId);

        //THEN
        assertThat(course1.getTitle()).isEqualTo(THIS_IS_MY_TITLE);
        assertThat(course1.getStatus()).isEqualTo(DRAFT);
        LocalDateTime createdAt = course1.getCreatedAt();
        assertThat(createdAt).isAfterOrEqualTo(beforeSave);
        assertThat(course1.getLastSavedAt()).isEqualTo(createdAt);
        assertThat(course1.getLastPublishedAt()).isNull();
        assertThat(course1.getPublishedAt()).isNull();
        assertThat(course1.getAuthor().getId()).isEqualTo(user.getId());
        assertThat(course1.getAuthor().getEmail()).isEqualTo(user.getEmail());
        assertThat(((NotFoundCourseProviderSpy) courseProvider).registerCourseCalled).isTrue();

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        Assertions.assertThat(linkCompanyCourseCaptured.getCompanyId()).isEqualTo(companyId);
        Assertions.assertThat(linkCompanyCourseCaptured.getCourseId()).isEqualTo(course1.getId());
        Assertions.assertThat(linkCompanyCourseCaptured.isExclusive()).isTrue();
        Assertions.assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        Assertions.assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        Assertions.assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();

        verify(checker).checkCompanyExistence(anyLong());
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When a company viewer or a non-company user initializes a company course, a forbidden action exception is thrown.")
    void shouldThrownException_whenInitializesCompanyCourse_asCompanyViewerOrNonCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(1);

        doNothing().when(checker).checkCompanyExistence(anyLong());
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.init(THIS_IS_MY_TITLE, user, 1));

        //THEN
        assertThat(((NotFoundCourseProviderSpy) courseProvider).registerCourseCalled).isFalse();
        verify(checker).checkCompanyExistence(anyLong());
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When a company does not exist and a user initializes a course for this company, a resource not found exception is thrown.")
    void shouldThrownException_whenUserInitializesCompanyCourse_ForNotExistingCompany() throws ResourceNotFoundException {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(1);

        doThrow(ResourceNotFoundException.class).when(checker).checkCompanyExistence(anyLong());

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.init(THIS_IS_MY_TITLE, user, 1));

        //THEN
        assertThat(((NotFoundCourseProviderSpy) courseProvider).registerCourseCalled).isFalse();
        verify(checker).checkCompanyExistence(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    void should_find_the_corresponding_courseBy_Id() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        User user = ZerofiltreUtilsTest.createMockUser(false);

        //WHEN
        Course found = courseService.findById(1, user);

        //THEN
        assertThat(((Found_Published_WithUnknownAuthor_CourseProviderSpy) courseProvider).courseOfIdCalled).isTrue();
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(45);
        assertThat(found.getTitle()).isEqualTo(TEST_COURSE_TITLE);
        assertThat(found.getStatus()).isEqualTo(PUBLISHED);
        assertThat(found.getAuthor().getId()).isEqualTo(21);
    }

    @Test
    void should_ThrowException_WhenCourseNotFound() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.findById(1, editor));

        //THEN
        assertThat(((NotFoundCourseProviderSpy) courseProvider).courseOfIdCalled).isTrue();
    }

    @Test
    void should_find_the_corresponding_courseBy_Id_for_company() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        ReflectionTestUtils.setField(courseService, "companyCourseService", companyCourseService);

        when(companyCourseService.find(any(User.class), anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyCourse()));

        //WHEN
        Course found = courseService.findByIdAndCompanyId(1, new User(), 1);

        //THEN
        assertThat(((Found_Published_WithUnknownAuthor_CourseProviderSpy) courseProvider).courseOfIdCalled).isTrue();
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(45);
        assertThat(found.getTitle()).isEqualTo(TEST_COURSE_TITLE);
        assertThat(found.getStatus()).isEqualTo(PUBLISHED);
        assertThat(found.getAuthor().getId()).isEqualTo(21);
    }

    @Test
    void should_ThrowException_WhenLinkNotFound() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.findByIdAndCompanyId(1, editor, 1));

        //THEN
        assertThat(((NotFoundCourseProviderSpy) courseProvider).courseOfIdCalled).isFalse();
    }

    @Test
    void should_ThrowException_WhenCompanyCourseNotFound() throws ForbiddenActionException {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        ReflectionTestUtils.setField(courseService, "companyCourseService", companyCourseService);

        when(companyCourseService.find(any(User.class), anyLong(), anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.findByIdAndCompanyId(1, editor, 1));
    }

    @Test
    @DisplayName("When a platform or company admin or a company editor wants to modify a company course, the course is modified.")
    void shouldModifiedCourse_whenWantModifyCompanyCourse_asPlatformOrCompanyAdminOrCompanyEditor() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        CourseService courseService = spy(new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider));

        User mockUser = ZerofiltreUtilsTest.createMockUser(true);
        doNothing().when(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        //WHEN
        courseService.save(course, mockUser, 1);

        //THEN
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When a platform user or a company viewer wants to modify a company course, a forbidden action exception is thrown.")
    void shouldThrowException_whenWantModifyCompanyCourse_asPlatformUserOrCompanyViewer() throws ForbiddenActionException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        CourseService courseService = spy(new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider));

        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.save(new Course(), new User(), 1));

        //THEN
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());
    }

    @Test
    void publishOrSave_should_ThrowForbiddenActionException_when_UserIsNotAuthorNorAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();
        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.save(new Course(), new User(), 0));

        //THEN
        assertThat(((Found_Published_WithUnknownAuthor_CourseProviderSpy) courseProvider).registerCourseCalled).isFalse();
    }

    @Test
    void publishOrSave_should_putInReview_ifAuthor_notAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(PUBLISHED);

        //WHEN
        Course result = courseService.save(course, editor, 0);

        //THEN
        assertThat(result.getStatus()).isEqualTo(IN_REVIEW);
    }

    @Test
    void publishOrSave_should_publish_ifIsAuthor_andIsAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(PUBLISHED);
        editor.getRoles().add("ROLE_ADMIN");


        //WHEN
        Course result = courseService.save(course, editor, 0);

        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_should_publish_ifNotAuthor_ButAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(PUBLISHED);
        editor.getRoles().add("ROLE_ADMIN");

        //WHEN
        Course result = courseService.save(course, editor, 0);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);

    }


    @Test
    void publishOrSave_should_ThrowResourceNotFoundException_ifTagsNotFound() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();

        tagProvider = new NotFoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(PUBLISHED);
        course.setTags(List.of(new Tag(1, "tag1"), new Tag(2, "tag2")));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.save(course, editor, 0));

        //THEN
        assertThat(((Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons) courseProvider).registerCourseCalled).isFalse();
        assertThat(((NotFoundTagProviderSpy) tagProvider).tagOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onSave() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);


        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor, 0);

        LocalDateTime afterSave = LocalDateTime.now();

        //THEN
        assertThat(result.getLastSavedAt()).isBetween(beforeSave, afterSave);
        assertThat(result.getPublishedAt()).isNull();
        assertThat(result.getLastPublishedAt()).isNull();
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onPublish_AlreadyPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(PUBLISHED);


        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor, 0);

        LocalDateTime afterSave = LocalDateTime.now();

        //THEN
        assertThat(result.getLastSavedAt()).isBetween(beforeSave, afterSave);
        assertThat(result.getPublishedAt()).isBefore(beforeSave);
        assertThat(result.getLastPublishedAt()).isEqualTo(result.getLastSavedAt());
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onReview() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(IN_REVIEW);

        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor, 0);

        LocalDateTime afterSave = LocalDateTime.now();

        //THEN
        assertThat(result.getLastSavedAt()).isBetween(beforeSave, afterSave);
        assertThat(result.getPublishedAt()).isNull();
        assertThat(result.getLastPublishedAt()).isNull();
        assertThat(result.getStatus()).isEqualTo(IN_REVIEW);
    }

    @Test
    void publishOrSave_should_setStatusToDraft_whenSaving_InReviewCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_InReview_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(DRAFT);

        //WHEN
        Course result = courseService.save(course, editor, 0);

        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_should_setStatusToPublished_whenSaving_PublishedCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(PUBLISHED);

        //WHEN
        Course result = courseService.save(course, editor, 0);

        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setStatus(DRAFT);

        //WHEN
        Course result = courseService.save(course, ZerofiltreUtilsTest.createMockUser(false), 0);

        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_byNonAdmin_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        Course result = courseService.save(course, editor, 0);

        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_shouldThrow_ResourceNotFoundException_whenTagNotFound() {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new NotFoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        course.setTags(List.of(new Tag(15, "tag")));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.save(course, editor, 0));
    }

    @Test
    void delete_ShouldThrow_ResourceNotFoundException_WhenCourseNotFound() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        ZerofiltreUtilsTest.createMockUser(true);

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.delete(15, new User()));
    }

    @Test
    void delete_ShouldThrow_ForbiddenActionException_WhenUserIsNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.delete(15, new User()));
    }

    @Test
    void delete_should_callTheProvider() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = mock(CourseProvider.class);
        User author = new User();
        author.setEmail(editor.getEmail());
        course = ZerofiltreUtilsTest.createMockCourse(true, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course.setEnrolledCount(0);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.ofNullable(course));

        tagProvider = new FoundTagProviderSpy();
        loggerProvider = new LoggerProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        courseService.delete(15, editor);

        //THEN
        verify(courseProvider, times(1)).courseOfId(anyLong());
        assertThat(((LoggerProviderSpy) loggerProvider).logCalled).isTrue();
    }

    @Test
    void findByIdOnDraft_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, new User()));
    }

    @Test
    void findByIdOnInReview_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_InReview_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, new User()));
    }

    @Test
    void findById_ReturnCourse_IfUserNull() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        Course result = courseService.findById(15, null);
        //THEN
        assertThat(result).isNotNull();
    }

    @Test
    void findById_ThrowsException_IfUserNull_AndCourseIsNotPublished() {
        //GIVEN
        courseProvider = new Found_InReview_WithUnknownAuthor_CourseProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN & THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, null));
    }

    @Test
    void deleteThrowsForbiddenActionException_whenEnrolledCount_NotZero_evenForAnAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        loggerProvider = new LoggerProviderSpy();
        editor.getRoles().add("ROLE_ADMIN");

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.delete(15, editor));
    }

    @Test
    void deleteThrowsForbiddenActionException_ifCourseIsPublished_evenForAnAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        loggerProvider = new LoggerProviderSpy();
        editor.getRoles().add("ROLE_ADMIN");

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.delete(15, editor))
                .withMessage("You are not allowed to delete this course as it is published");
    }
}