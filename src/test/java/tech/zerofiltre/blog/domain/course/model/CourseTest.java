package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;
import static tech.zerofiltre.blog.util.ZerofiltreUtils.*;

class CourseTest {

    public static final String THIS_IS_MY_TITLE = "This is my title";
    public static final String UPDATED_TITLE = "This is the updated title";
    public static final String UPDATED_SUB_TITLE = "This is the updated sub title";
    public static final String UPDATED_SUMMARY = "This is the updated summary";
    public static final String UPDATED_VIDEO = "updated video";
    public static final String UPDATED_THUMBNAIL = "updated thumbnail";
    private Course course;
    private CourseProvider courseProvider;
    private UserProvider userProvider;
    private TagProvider tagProvider;

    private SectionProvider sectionProvider;
    private LoggerProvider loggerProvider;

    @Test
    void should_init_course_properly() throws UserNotFoundException {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();
        userProvider = new FoundNonAdminUserProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)

                .build();
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(1);
        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        course.init(THIS_IS_MY_TITLE, user);

        //THEN
        assertThat(course.getTitle()).isEqualTo(THIS_IS_MY_TITLE);
        assertThat(course.getStatus()).isEqualTo(DRAFT);
        LocalDateTime createdAt = course.getCreatedAt();
        assertThat(createdAt).isAfterOrEqualTo(beforeSave);
        assertThat(course.getLastSavedAt()).isEqualTo(createdAt);
        assertThat(course.getLastPublishedAt()).isNull();
        assertThat(course.getPublishedAt()).isNull();
        assertThat(course.getAuthor().getId()).isEqualTo(user.getId());
        assertThat(course.getAuthor().getEmail()).isEqualTo(user.getEmail());
        assertThat(((NotFoundCourseProviderSpy) courseProvider).registerCourseCalled).isTrue();
    }

    @Test
    void should_find_the_corresponding_courseBy_Id() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();
        userProvider = new FoundNonAdminUserProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .build();
        User user = ZerofiltreUtils.createMockUser(false);

        //WHEN
        Course found = course.findById(1, user);

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
        userProvider = new FoundNonAdminUserProviderSpy();
        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .build();
        User user = ZerofiltreUtils.createMockUser(false);

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> course.findById(1, user));

        //THEN
        assertThat(((NotFoundCourseProviderSpy) courseProvider).courseOfIdCalled).isTrue();
    }

    @Test
    void init_should_ThrowException_when_UserNotFound() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new NotFoundUserProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)

                .build();
        User user = ZerofiltreUtils.createMockUser(true);

        //WHEN
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> course.init(THIS_IS_MY_TITLE, user));

        //THEN
        assertThat(((Found_Published_WithKnownAuthor_CourseProvider_Spy) courseProvider).registerCourseCalled).isFalse();
        assertThat(((NotFoundUserProviderSpy) userProvider).userOfIdCalled).isTrue();
    }

    @Test
    void findById_should_ThrowException_when_UserNotFound() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new NotFoundUserProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)

                .build();
        User user = ZerofiltreUtils.createMockUser(true);

        //WHEN
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> course.findById(1, user));

        //THEN
        assertThat(((Found_Published_WithKnownAuthor_CourseProvider_Spy) courseProvider).registerCourseCalled).isFalse();
        assertThat(((NotFoundUserProviderSpy) userProvider).userOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_ThrowForbiddenActionException_when_UserIsNotAuthorNorAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .tagProvider(tagProvider)
                .userProvider(userProvider)

                .build();

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> course.save(15));

        //THEN
        assertThat(((Found_Published_WithUnknownAuthor_CourseProviderSpy) courseProvider).registerCourseCalled).isFalse();
        assertThat(((FoundNonAdminUserProviderSpy) userProvider).userOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_putInReview_ifAuthor_notAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .tagProvider(tagProvider)
                .userProvider(userProvider)
                .status(PUBLISHED)
                .build();

        //WHEN
        Course result = course.save(15);

        //THEN
        assertThat(result.getStatus()).isEqualTo(IN_REVIEW);
    }

    @Test
    void publishOrSave_should_publish_ifAuthor_isAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .tagProvider(tagProvider)
                .userProvider(userProvider)
                .status(PUBLISHED)
                .build();

        //WHEN
        Course result = course.save(15);

        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_should_publish_ifNotAuthor_ButAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .tagProvider(tagProvider)
                .userProvider(userProvider)
                .status(PUBLISHED)
                .build();

        //WHEN
        Course result = course.save(15);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);

    }

    @Test
    void publishOrSave_should_set_inReview_ifNotAuthor_ButAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_InReview_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .tagProvider(tagProvider)
                .userProvider(userProvider)
                .status(PUBLISHED)
                .build();

        //WHEN
        Course result = course.save(15);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);

    }

    @Test
    void publishOrSave_should_ThrowResourceNotFoundException_ifTagsNotFound() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new NotFoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .tags(Collections.singletonList(new Tag(1, "tag1")))
                .build();

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> course.save(15));

        //THEN
        assertThat(((Found_Published_WithKnownAuthor_CourseProvider_Spy) courseProvider).registerCourseCalled).isFalse();
        assertThat(((FoundAdminUserProviderSpy) userProvider).userOfIdCalled).isTrue();
        assertThat(((NotFoundTagProviderSpy) tagProvider).tagOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onSave() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)

                .build();
        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = course.save(15);

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
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)

                .build();
        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = course.save(15);

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
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .status(PUBLISHED)
                .build();
        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = course.save(15);

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
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)

                .build();

        //WHEN
        Course result = course.save(15);


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_should_setStatusToPublished_whenSaving_PublishedCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)

                .build();

        //WHEN
        Course result = course.save(15);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();


        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();

        //WHEN
        Course result = course.save(15);


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);

    }

    @Test
    void publishOrSave_byNonAdmin_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();

        //WHEN
        Course result = course.save(15);


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);

    }

    @Test
    void publishOrSave_shouldThrow_ResourceNotFoundException_whenTagNotFound() {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new NotFoundTagProviderSpy();
        sectionProvider = new SectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .tags(Collections.singletonList(new Tag(1, "tag")))
                .build();

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> course.save(15));


    }

    @Test
    void delete_ShouldThrow_ResourceNotFoundException_WhenCourseNotFound() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();

        ZerofiltreUtils.createMockUser(true);


        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> course.delete(15, new User()));

    }

    @Test
    void delete_ShouldThrow_ForbiddenActionException_WhenUserIsNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();


        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> course.delete(15, new User()));
    }

    @Test
    void delete_should_callTheProvider() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        userProvider = new FoundAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();
        loggerProvider = new LoggerProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .loggerProvider(loggerProvider)
                .build();

        //WHEN
        course.delete(15, new User());

        //THEN
        assertThat(((Found_Draft_WithKnownAuthor_CourseProvider_Spy) courseProvider).deleteCalled).isTrue();
        assertThat(((LoggerProviderSpy) loggerProvider).logCalled).isTrue();
    }

    @Test
    void findByIdOnDraft_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> course.findById(15, new User()));
    }

    @Test
    void findByIdOnInReview_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_InReview_WithUnknownAuthor_CourseProviderSpy();
        userProvider = new FoundNonAdminUserProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> course.findById(15, new User()));
    }

    @Test
    void findById_ReturnCourse_IfUserNull() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();

        //WHEN
        Course result = course.findById(15, null);
        //THEN
        assertThat(result).isNotNull();
    }

    @Test
    void findById_ThrowsException_IfUserNull_AndCourseIsNotPublished() {
        //GIVEN
        courseProvider = new Found_InReview_WithUnknownAuthor_CourseProviderSpy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build();

        //WHEN & THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> course.findById(15, null));
    }

}