package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.*;

class LessonTest {

    public static final String CONTENT_TO_SAVE = "content to save";
    public static final String TITLE_TO_SAVE = "Lesson 8";
    public static final String THUMBNAIL_TO_SAVE = "thumbnail";
    public static final String SUMMARY_TO_SAVE = "summary";
    public static final String VIDEO_TO_SAVE = "video";
    public static final String CONTENT = "Content";
    public static final String VIDEO = "Video";

    @Test
    void init_throws_ResourceNotFoundException_if_author_not_found() {
        //given
        Lesson lesson = Lesson.builder()
                .userProvider(new NotFoundUserProviderSpy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lesson.init("Lesson 1", 1, 100));
    }

    @Test
    void init_throws_ResourceNotFoundException_if_chapter_not_found() {
        //given
        Lesson lesson = Lesson.builder()
                .userProvider(new FoundNonAdminUserProviderSpy())
                .chapterProvider(new NotFoundChapterProviderSpy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lesson.init("Lesson 1", 1, 100));
    }

    @Test
    void init_throws_ForbiddenActionException_if_user_is_not_admin_nor_course_author() {
        //given
        Lesson lesson = Lesson.builder()
                .userProvider(new FoundNonAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .lessonProvider(new FoundLessonProviderSpy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.init("Lesson 1", 1, 100));

    }

    @Test
    void init_worksProperly_if_user_is_admin_and_not_author() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Found_Draft_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        Lesson lesson = Lesson.builder()
                .title("Lesson 1")
                .chapterId(1)
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(chapterProvider)
                .courseProvider(courseProvider)
                .lessonProvider(new FoundLessonProviderSpy())
                .build();

        //when
        lesson.init("Lesson 1", 1, 100);

        //then
        org.assertj.core.api.Assertions.assertThat(lesson.getTitle()).isEqualTo("Lesson 1");
        org.assertj.core.api.Assertions.assertThat(lesson.getChapterId()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(lesson.getType()).isEqualTo("text");
        org.assertj.core.api.Assertions.assertThat(chapterProvider.chapterOfIdCalled).isTrue();
        org.assertj.core.api.Assertions.assertThat(courseProvider.courseOfIdCalled).isTrue();
    }

    @Test
    void init_defines_lesson_number() throws ForbiddenActionException, ResourceNotFoundException {
        //given

        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        Lesson firstLesson = Lesson.builder().id(1).number(1).build();
        Lesson secondLesson = Lesson.builder().id(2).number(2).build();
        Chapter chapter = Chapter.builder()
                .lessons(List.of(firstLesson, secondLesson))
                .build();
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserProvider userProvider = mock(UserProvider.class);
        User user = ZerofiltreUtils.createMockUser(true);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Lesson lesson = Lesson.builder()
                .title("Lesson 1")
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .courseProvider(courseProvider)
                .lessonProvider(lessonProvider)
                .build();

        //when
        lesson = lesson.init("Git basis", 1, 100);

        //then
        org.assertj.core.api.Assertions.assertThat(lesson.getNumber()).isEqualTo(3);
        verify(chapterProvider, times(1)).chapterOfId(anyLong());
        verify(courseProvider, times(1)).courseOfId(anyLong());
    }

    @Test
    void save_throws_ResourceNotFoundException_if_author_not_found() {
        //given
        Lesson lesson = Lesson.builder()
                .userProvider(new NotFoundUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .lessonProvider(new FoundLessonProviderSpy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lesson.save(100));
    }

    @Test
    void save_throws_ResourceNotFoundException_if_chapter_not_found() {
        //given
        Lesson lesson = Lesson.builder()
                .userProvider(new FoundNonAdminUserProviderSpy())
                .chapterProvider(new NotFoundChapterProviderSpy())
                .lessonProvider(new FoundLessonProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lesson.save(100));

    }

    @Test
    void save_throws_ForbiddenActionException_if_user_is_not_admin_nor_course_author() {
        //given
        Lesson lesson = Lesson.builder()
                .userProvider(new FoundNonAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .lessonProvider(new FoundLessonProviderSpy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.save(100));
    }

    @Test
    void save_throws_ResourceNotFound_if_lesson_not_found() {
        //given
        Lesson lesson = Lesson.builder()
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .lessonProvider(new NotFoundLessonProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lesson.save(100));
    }

    @Test
    void save_worksProperly_if_user_is_admin_and_not_author() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Found_Draft_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        Lesson lesson = Lesson.builder()
                .title(TITLE_TO_SAVE)
                .id(1500)
                .chapterId(561)
                .content(CONTENT_TO_SAVE)
                .thumbnail(THUMBNAIL_TO_SAVE)
                .free(true)
                .summary(SUMMARY_TO_SAVE)
                .video(VIDEO_TO_SAVE)
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(chapterProvider)
                .courseProvider(courseProvider)
                .lessonProvider(new FoundLessonProviderSpy())
                .build();

        //when
        Lesson saved = lesson.save(100);

        //then
        org.assertj.core.api.Assertions.assertThat(saved.getId()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(saved.getTitle()).isEqualTo(TITLE_TO_SAVE);
        org.assertj.core.api.Assertions.assertThat(saved.getContent()).isEqualTo(CONTENT_TO_SAVE);
        org.assertj.core.api.Assertions.assertThat(saved.getType()).isEqualTo("video");
        org.assertj.core.api.Assertions.assertThat(saved.getThumbnail()).isEqualTo(THUMBNAIL_TO_SAVE);
        org.assertj.core.api.Assertions.assertThat(saved.isFree()).isTrue();
        org.assertj.core.api.Assertions.assertThat(saved.getSummary()).isEqualTo(SUMMARY_TO_SAVE);
        org.assertj.core.api.Assertions.assertThat(saved.getVideo()).isEqualTo(VIDEO_TO_SAVE);
        org.assertj.core.api.Assertions.assertThat(saved.getChapterId()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(chapterProvider.chapterOfIdCalled).isTrue();
        org.assertj.core.api.Assertions.assertThat(courseProvider.courseOfIdCalled).isTrue();
    }

    @Test
    void delete_throws_ResourceNotFound_if_lesson_not_found() {
        //given
        Lesson lesson = Lesson.builder()
                .id(1)
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .lessonProvider(new NotFoundLessonProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lesson.delete(100));
    }

    @Test
    void delete_throws_ForbiddenActionException_if_user_is_not_author_nor_admin() {
        //given
        Lesson lesson = Lesson.builder()
                .id(1)
                .userProvider(new FoundNonAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .lessonProvider(new FoundLessonProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.delete(100));
    }

    @Test
    void delete_works_if_user_is_not_author_but_is_admin() throws ForbiddenActionException, ResourceNotFoundException {

        FoundLessonProviderSpy lessonProvider = new FoundLessonProviderSpy();
        Lesson lesson = Lesson.builder()
                .id(1)
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .lessonProvider(lessonProvider)
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .build();
        lesson.delete(100);

        //then
        org.assertj.core.api.Assertions.assertThat(lessonProvider.deleteCalled).isTrue();
    }

    @Test
    void onlyAdminCanDeleteLesson_ofPublishedCourse() {
        //given
        UserProvider userProvider = mock(UserProvider.class);
        User author = new User();
        author.setId(999);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(author));

        CourseProvider courseProvider = mock(CourseProvider.class);
        Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        Lesson lesson = Lesson.builder()
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.delete(999));
    }

    @Test
    void get_throws_ForbiddenActionException_if_not_admin_nor_author_and_course_not_published() {
        //given
        UserProvider userProvider = mock(UserProvider.class);
        User user = new User();
        user.setId(999);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        CourseProvider courseProvider = mock(CourseProvider.class);
        Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        Lesson lesson = Lesson.builder()
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .courseProvider(courseProvider)
                .lessonProvider(lessonProvider)
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.get(999))
                .withMessage(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
    }

    @Test
    void get_Throws_ResourceNotFound_if_chapter_not_found_for_not_connected_user() {
        //given
        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(10)).thenReturn(Optional.empty());

        LessonProvider lessonProvider = mock(LessonProvider.class);
        Lesson built = Lesson.builder()
                .chapterId(10)
                .build();

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(built));

        Optional<Lesson> lessonOptional = Optional.ofNullable(
                Lesson.builder()
                        .lessonProvider(lessonProvider)
                        .chapterProvider(chapterProvider)
                        .build()
        );

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonOptional.get().get(0))
                .withMessageContaining("The chapter with id: " + 10 + DOES_NOT_EXIST);
    }


    @Test
    void get_throws_ForbiddenActionException_if_not_connected_and_course_not_published() {
        //given
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        Lesson lesson = Lesson.builder()
                .chapterProvider(chapterProvider)
                .courseProvider(courseProvider)
                .lessonProvider(lessonProvider)
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.get(0))
                .withMessage(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
    }

    @Test
    void get_throws_ResourceNotFound_if_lesson_not_found() {
        //given
        Lesson lesson = Lesson.builder()
                .id(1)
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .lessonProvider(new NotFoundLessonProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .build();

        //when
        //then
        org.assertj.core.api.Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lesson.get(12));
    }

    @Test
    void get_returns_lesson_with_all_data() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        Lesson lesson = Lesson.builder()
                .id(1)
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .lessonProvider(new FoundLessonProviderSpy())
                .enrollmentProvider(new EnrollmentProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .build();

        //when
        Lesson result = lesson.get(12);

        //then
        org.assertj.core.api.Assertions.assertThat(result.getId()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(result.getTitle()).isEqualTo("Lesson 1");
        org.assertj.core.api.Assertions.assertThat(result.getChapterId()).isEqualTo(1);

    }

    @Test
    void get_NonFreeLesson_returns_content_exceptVideo_ifNotPartOfEnrollment() throws ResourceNotFoundException, ForbiddenActionException {
        //given

        UserProvider userProvider = mock(UserProvider.class);
        User user = new User();
        user.setId(999);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        User author = ZerofiltreUtils.createMockUser(false);
        Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));


        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        Lesson lesson = Lesson.builder()
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .build();

        //when
        Lesson result = lesson.get(0);

        //then
        org.assertj.core.api.Assertions.assertThat(result.getId()).isEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(result.getType()).isEqualTo("video");
        org.assertj.core.api.Assertions.assertThat(result.getContent()).isEqualTo(CONTENT);
        org.assertj.core.api.Assertions.assertThat(result.getVideo()).isEqualTo(VIDEO_NOT_AVAILABLE_FOR_FREE);

    }

    @Test
    void get_NonFreeLesson_returns_content_exceptVideo_ifNotConnected() throws ResourceNotFoundException, ForbiddenActionException {
        //given

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        User author = ZerofiltreUtils.createMockUser(false);
        Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));


        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        Lesson lesson = Lesson.builder()
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .build();

        //when
        Lesson result = lesson.get(0);

        //then
        org.assertj.core.api.Assertions.assertThat(result.getId()).isEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(result.getType()).isEqualTo("video");
        org.assertj.core.api.Assertions.assertThat(result.getContent()).isEqualTo(CONTENT);
        org.assertj.core.api.Assertions.assertThat(result.getVideo()).isEqualTo(VIDEO_NOT_AVAILABLE_FOR_FREE);

    }

    @Test
    void get_freeLesson_returns_fullContent_evenNotPartOfEnrollment() throws ResourceNotFoundException, ForbiddenActionException {
        //given

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).free(true).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        User author = ZerofiltreUtils.createMockUser(false);
        Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));

        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        Lesson lesson = Lesson.builder()
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .build();

        //when
        Lesson result = lesson.get(0);

        //then
        org.assertj.core.api.Assertions.assertThat(result.getId()).isEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(result.getType()).isEqualTo("video");
        org.assertj.core.api.Assertions.assertThat(result.getContent()).isEqualTo(CONTENT);
        org.assertj.core.api.Assertions.assertThat(result.getVideo()).isEqualTo(VIDEO);

    }

    @Test
    void get_Lesson_returns_fullContent_evenNotPartOfEnrollment_andAdmin() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        User currentUser = new User();
        currentUser.setId(85);
        currentUser.getRoles().add("ROLE_ADMIN");
        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.userOfId(85)).thenReturn(Optional.of(currentUser));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        User author = ZerofiltreUtils.createMockUser(false);
        Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));

        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        Lesson lesson = Lesson.builder()
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .build();

        //when
        Lesson result = lesson.get(85);

        //then
        org.assertj.core.api.Assertions.assertThat(result.getId()).isEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(result.getType()).isEqualTo("video");
        org.assertj.core.api.Assertions.assertThat(result.getContent()).isEqualTo(CONTENT);
        org.assertj.core.api.Assertions.assertThat(result.getVideo()).isEqualTo(VIDEO);

    }

}