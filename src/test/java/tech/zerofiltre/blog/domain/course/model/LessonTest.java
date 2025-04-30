package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
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
        User user = ZerofiltreUtilsTest.createMockUser(true);
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
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
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
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
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
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
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
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
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
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
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
        UserProvider userProvider = mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(999);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).free(true).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
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
        Lesson result = lesson.get(5);

        //then
        org.assertj.core.api.Assertions.assertThat(result.getId()).isEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(result.getType()).isEqualTo("video");
        org.assertj.core.api.Assertions.assertThat(result.getContent()).isEqualTo(CONTENT);
        org.assertj.core.api.Assertions.assertThat(result.getVideo()).isEqualTo(VIDEO);

    }
    @Test
    void get_freeLesson_returns_fullContent_evenNotConnected() throws ResourceNotFoundException, ForbiddenActionException {
        //given

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(10).courseId(18).build()));

        LessonProvider lessonProvider = mock(LessonProvider.class);
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.ofNullable(Lesson.builder().id(20).chapterId(10).free(true).title("Lesson 1").content(CONTENT).video(VIDEO).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        mockCourse.setId(18);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));

        Lesson lesson = Lesson.builder()
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
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
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course mockCourse = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
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

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of an unpublished company-owned course for a viewer user, a forbidden action exception is thrown.")
    void shouldThrownException_whenCheckAccessConditions_forLessonPartUnpublishedCompanyOwnedCourse_forCompanyViewer() throws ForbiddenActionException {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = Mockito.mock(DataChecker.class);
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .checker(checker)
                .title("Lesson 1")
                .build();

        //WHEN
        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.checkLessonAccessConditions(1, 1, false, false));

        verify(courseProvider).courseOfId(anyLong());
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson and it is an platform admin, there's nothing there.")
    void shouldNothing_whenCheckAccessConditions_forLessonAndPlatformAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        DataChecker checker = Mockito.mock(DataChecker.class);

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons())
                .checker(checker)
                .enrollmentProvider(new EnrollmentProviderSpy())
                .title("Lesson 1")
                .free(true)
                .build();

        //WHEN
        lesson.checkLessonAccessConditions(1, 1, true, true);

        //THEN
        verify(checker, never()).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published company-owned course for a viewer user and deletion is in progress, a forbidden action exception is thrown.")
    void shouldThrownException_whenCheckAccessConditions_forLessonPartPublishedCompanyOwnedCourse_forCompanyViewer_andDeletionInProgress() {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = Mockito.mock(DataChecker.class);
        when(checker.isCompanyAdmin(anyLong(), anyLong())).thenReturn(false);

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .checker(checker)
                .title("Lesson 1")
                .build();

        //WHEN
        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.checkLessonAccessConditions(1, 1, true, true))
                .withMessage("You can not delete a lesson that is already published");

        verify(courseProvider).courseOfId(anyLong());
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).isCompanyAdmin(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published company-owned course for a company admin and deletion is in progress, there's nothing there.")
    void shouldNothing_whenCheckAccessConditions_forLessonPartPublishedCompanyOwnedCourse_forCompanyAdmin_andDeletionInProgress() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = Mockito.mock(DataChecker.class);
        when(checker.isCompanyAdmin(anyLong(), anyLong())).thenReturn(true);

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .checker(checker)
                .enrollmentProvider(new EnrollmentProviderSpy())
                .title("Lesson 1")
                .build();

        //WHEN
        lesson.checkLessonAccessConditions(1, 1, true, true);

        //THEN
        verify(courseProvider).courseOfId(anyLong());
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).isCompanyAdmin(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published course, it is an non-admin user, not the author and the verification of the enrollments is false, a forbidden action exception is thrown.")
    void shouldThrownException_whenCheckAccessConditions_forLessonAndNonAdminUserAndNotAuthorAndCheckEnrollmentsIsFalse() {
        //GIVEN
        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(new Found_Published_WithUnknownAuthor_CourseProviderSpy())
                .enrollmentProvider(new EnrollmentProviderSpy())
                .title("Lesson 1")
                .free(true)
                .build();

        //WHEN
        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.checkLessonAccessConditions(1, 1, false, false))
                .withMessage("You are not allowed to do this action on this course");
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published course, it is an non-admin user and the author and the verification of the enrollments is false, there's nothing there.")
    void shouldNothing_whenCheckAccessConditions_forLessonAndNonAdminUserAndIsAuthorAndCheckEnrollmentsIsFalse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        UserProvider userProvider = mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(false);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .title("Lesson 1")
                .build();

        //WHEN
        lesson.checkLessonAccessConditions(1, 1, false, false);

        //THEN
        assertThat(course.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(user.isAdmin()).isFalse();
        assertThat(user).isEqualTo(course.getAuthor());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published company-owned course, it is an non-admin user and the verification of the enrollments is false, a forbidden action exception is thrown.")
    void shouldThrownException_whenCheckAccessConditions_forLessonPartOfPublishedCompanyOwnedCourse_andNonCompanyAdminAndCheckEnrollmentsIsFalse() throws ForbiddenActionException {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = Mockito.mock(DataChecker.class);
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .checker(checker)
                .title("Lesson 1")
                .build();

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.checkLessonAccessConditions(1, 1, false, false));

        //THEN
        verify(courseProvider).courseOfId(anyLong());
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published course, it is an non-admin user and not author, and the verification of the enrollments is true, the non-access enrolled is true.")
    void shouldNotAccessEnrolledIsTrue_whenCheckAccessConditions_forLessonAndNonAdminUserAndNotAuthorAndCheckEnrollmentsIsTrue() throws ForbiddenActionException, ResourceNotFoundException, NoSuchFieldException, IllegalAccessException {
        //GIVEN
        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(new Found_Published_WithUnknownAuthor_CourseProviderSpy())
                .enrollmentProvider(enrollmentProvider)
                .title("Lesson 1")
                .build();

        //WHEN
        lesson.checkLessonAccessConditions(1, 1, false, true);

        //THEN
        Field notEnrolledAccessField = Lesson.class.getDeclaredField("notEnrolledAccess");
        notEnrolledAccessField.setAccessible(true);
        assertThat(notEnrolledAccessField.get(lesson)).isEqualTo(true);
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published course, it is an non-admin user and author, and the verification of the enrollments is true, the non-access enrolled is false.")
    void shouldNotAccessEnrolledIsFalse_whenCheckAccessConditions_forLessonAndNonAdminUserAndIsAuthorAndCheckEnrollmentsIsTrue() throws ForbiddenActionException, ResourceNotFoundException, NoSuchFieldException, IllegalAccessException {
        //GIVEN
        UserProvider userProvider = mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(false);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.empty());

        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .title("Lesson 1")
                .build();

        //WHEN
        lesson.checkLessonAccessConditions(1, 1, false, true);

        //THEN
        Field notEnrolledAccessField = Lesson.class.getDeclaredField("notEnrolledAccess");
        notEnrolledAccessField.setAccessible(true);
        assertThat(notEnrolledAccessField.get(lesson)).isEqualTo(false);
        verify(enrollmentProvider).enrollmentOf(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published company-owned course, it is a company user and the verification of the enrollments is true, there's nothing there.")
    void shouldNothing_whenCheckAccessConditions_forLessonPartOfPublishedCompanyOwnedCourse_andCompanyUserAndCheckEnrollmentsIsTrue() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        DataChecker checker = Mockito.mock(DataChecker.class);

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .checker(checker)
                .title("Lesson 1")
                .build();

        //WHEN
        lesson.checkLessonAccessConditions(1, 1, false, true);

        //THEN
        verify(courseProvider).courseOfId(anyLong());
        verify(enrollmentProvider).enrollmentOf(anyLong(), anyLong(), anyBoolean());
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When I check the access conditions for a lesson that is part of a published company-owned course, the user is a non-platform admin or non-company user and the verification of the enrollments is true, a forbidden action exception is thrown.")
    void shouldThrownException_whenCheckAccessConditions_forLessonPartOfPublishedCompanyOwnedCourse_andUserIsNotPlatformAdminOrCompanyUserAndCheckEnrollmentsIsTrue() throws ForbiddenActionException {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        EnrollmentProvider enrollmentProvider = mock(EnrollmentProvider.class);
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        DataChecker checker = Mockito.mock(DataChecker.class);
        doThrow(ForbiddenActionException.class).when(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .checker(checker)
                .title("Lesson 1")
                .build();

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.checkLessonAccessConditions(1, 1, false, true));

        //THEN
        verify(courseProvider).courseOfId(anyLong());
        verify(enrollmentProvider).enrollmentOf(anyLong(), anyLong(), anyBoolean());
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When a user who is not logged in wants to access a lesson that is part of a published company-owned course, a forbidden action exception is thrown.")
    void shouldThrownException_whenUserNotLogged_wantsAccessLessonCompanyOwnedCourse() {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        Lesson lesson = Lesson.builder()
                .id(1)
                .lessonProvider(new FoundLessonProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .courseProvider(courseProvider)
                .title("Lesson 1")
                .build();

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lesson.get(0))
                .withMessage("You are not allowed to do this action on this course");

        //THEN
        verify(courseProvider).courseOfId(anyLong());
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
    }

}