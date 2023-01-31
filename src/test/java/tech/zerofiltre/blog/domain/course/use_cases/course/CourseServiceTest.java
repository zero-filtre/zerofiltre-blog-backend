package tech.zerofiltre.blog.domain.course.use_cases.course;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;
import static tech.zerofiltre.blog.util.ZerofiltreUtils.*;

class CourseServiceTest {

    public static final String THIS_IS_MY_TITLE = "This is my title";
    public static final String UPDATED_TITLE = "This is the updated title";
    public static final String UPDATED_SUB_TITLE = "This is the updated sub title";
    public static final String UPDATED_SUMMARY = "This is the updated summary";
    public static final String UPDATED_VIDEO = "updated video";
    public static final String UPDATED_THUMBNAIL = "updated thumbnail";

    private CourseProvider courseProvider;
    private TagProvider tagProvider;
    private SectionProvider sectionProvider;
    private ChapterProvider chapterProvider;
    private LoggerProvider loggerProvider;
    private Course course;
    private User editor;

    @BeforeEach
    void init() {
        course = new Course();
        editor = ZerofiltreUtils.createMockUser(false);
        chapterProvider = new FoundChapterProviderSpy();
    }


    @Test
    void should_init_course_properly() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(1);
        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course course = courseService.init(THIS_IS_MY_TITLE, user);

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
        chapterProvider = new FoundChapterProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        User user = ZerofiltreUtils.createMockUser(false);

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

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.findById(1, editor));

        //THEN
        assertThat(((NotFoundCourseProviderSpy) courseProvider).courseOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_ThrowForbiddenActionException_when_UserIsNotAuthorNorAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();
        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.save(new Course(), new User()));

        //THEN
        assertThat(((Found_Published_WithUnknownAuthor_CourseProviderSpy) courseProvider).registerCourseCalled).isFalse();
    }

    @Test
    void publishOrSave_should_putInReview_ifAuthor_notAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(PUBLISHED);

        //WHEN
        Course result = courseService.save(course, editor);

        //THEN
        assertThat(result.getStatus()).isEqualTo(IN_REVIEW);
    }

    @Test
    void publishOrSave_should_publish_ifIsAuthor_andIsAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(PUBLISHED);
        editor.getRoles().add("ROLE_ADMIN");


        //WHEN
        Course result = courseService.save(course, editor);

        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_should_publish_ifNotAuthor_ButAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(PUBLISHED);
        editor.getRoles().add("ROLE_ADMIN");

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);

    }


    @Test
    void publishOrSave_should_ThrowResourceNotFoundException_ifTagsNotFound() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new NotFoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(PUBLISHED);
        course.setTags(List.of(new Tag(1, "tag1"), new Tag(2, "tag2")));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.save(course, editor));

        //THEN
        assertThat(((Found_Published_WithKnownAuthor_CourseProvider_Spy) courseProvider).registerCourseCalled).isFalse();
        assertThat(((NotFoundTagProviderSpy) tagProvider).tagOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onSave() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);


        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor);

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

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(PUBLISHED);


        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor);

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

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(IN_REVIEW);

        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor);

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

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(DRAFT);

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_should_setStatusToPublished_whenSaving_PublishedCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(PUBLISHED);

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setStatus(DRAFT);

        //WHEN
        Course result = courseService.save(course, ZerofiltreUtils.createMockUser(false));


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);

    }

    @Test
    void publishOrSave_byNonAdmin_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);

    }

    @Test
    void publishOrSave_shouldThrow_ResourceNotFoundException_whenTagNotFound() {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new NotFoundTagProviderSpy();
        sectionProvider = new SectionProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course.setTags(List.of(new Tag(15, "tag")));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.save(course, editor));


    }

    @Test
    void delete_ShouldThrow_ResourceNotFoundException_WhenCourseNotFound() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        ZerofiltreUtils.createMockUser(true);


        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.delete(15, new User()));

    }

    @Test
    void delete_ShouldThrow_ForbiddenActionException_WhenUserIsNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.delete(15, new User()));
    }

    @Test
    void delete_should_callTheProvider() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();
        loggerProvider = new LoggerProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN
        courseService.delete(15, editor);

        //THEN
        assertThat(((Found_Draft_WithKnownAuthor_CourseProvider_Spy) courseProvider).deleteCalled).isTrue();
        assertThat(((LoggerProviderSpy) loggerProvider).logCalled).isTrue();
    }

    @Test
    void findByIdOnDraft_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, new User()));
    }

    @Test
    void findByIdOnInReview_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_InReview_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, new User()));
    }

    @Test
    void findById_ReturnCourse_IfUserNull() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();
        chapterProvider = new FoundChapterProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

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
        sectionProvider = new FoundSectionProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN & THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, null));
    }

    @Test
    void getLessonsCount_returns_theProperNumber(){
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        tagProvider = new FoundTagProviderSpy();
        sectionProvider = new FoundSectionProviderSpy();
        chapterProvider = new FoundChapterProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //WHEN
        int result = courseService.getLessonsCount(15);

        //THEN
        assertThat(result).isEqualTo(2);
    }

    @Test
    void getEnrolledCount_returns_theProperNumber() {
        //given
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        //when
        int result = courseService.getEnrolledCount(15);

        //then
        assertThat(result).isEqualTo(2);


    }

}