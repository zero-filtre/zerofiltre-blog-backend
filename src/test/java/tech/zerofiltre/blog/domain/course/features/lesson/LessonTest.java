package tech.zerofiltre.blog.domain.course.features.lesson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonTest {

    public static final String CONTENT_TO_SAVE = "content to save";
    public static final String TITLE_TO_SAVE = "Lesson 8";
    public static final String THUMBNAIL_TO_SAVE = "thumbnail";
    public static final String SUMMARY_TO_SAVE = "summary";
    public static final String VIDEO_TO_SAVE = "video";
    public static final String CONTENT = "Content";
    public static final String VIDEO = "Video";

    private LessonService lessonService;
    private Lesson lesson;

    @Mock
    LessonProvider lessonProvider;

    @Mock
    ChapterProvider chapterProvider;

    @Mock
    UserProvider userProvider;

    @Mock
    CourseProvider courseProvider;

    @Mock
    EnrollmentProvider enrollmentProvider;

    @BeforeEach
    void setup() {
        lessonService = new LessonService(lessonProvider, chapterProvider, userProvider, courseProvider, enrollmentProvider);
    }

    @Test
    void init_throws_ResourceNotFoundException_if_author_not_found() {
        //given
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.init("Lesson 1", 1, 100));
    }

    @Test
    void init_throws_ResourceNotFoundException_if_chapter_not_found() {
        //given
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(new User()));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.init("Lesson 1", 1, 100));
    }

    @Test
    void init_throws_ForbiddenActionException_if_user_is_not_admin_nor_course_author() {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(false);
        User author = new User();
        author.setId(user.getId() + 4);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockCourse(true, Status.DRAFT, author, new ArrayList<>(), new ArrayList<>())));

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.init("Lesson 1", 1, 100));

    }

    @Test
    void init_worksProperly_if_user_is_admin_and_not_author() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(true);
        User author = new User();
        author.setId(user.getId() + 4);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockCourse(true, Status.DRAFT, author, new ArrayList<>(), new ArrayList<>())));

        //when
        lessonService.init("Lesson 1", 1, 100);

        //then

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonProvider).save(captor.capture());
        Lesson captured = captor.getValue();

        assertThat(captured.getTitle()).isEqualTo("Lesson 1");
        assertThat(captured.getChapterId()).isEqualTo(1);

        verify(chapterProvider, times(2)).chapterOfId(anyLong());
        verify(courseProvider).courseOfId(anyLong());
    }

    @Test
    void init_defines_lesson_number() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(true);

        Lesson firstLesson = new Lesson();
        firstLesson.setId(1);
        firstLesson.setNumber(1);

        Lesson secondLesson = new Lesson();
        secondLesson.setId(2);
        secondLesson.setNumber(2);

        Chapter chapter = Chapter.builder()
                .id(4L)
                .lessons(List.of(firstLesson, secondLesson))
                .build();

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList())));

        when(lessonProvider.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        lesson = lessonService.init("Git basis", chapter.getId(), user.getId());

        //then
        assertThat(lesson.getNumber()).isEqualTo(3);
        verify(chapterProvider, times(2)).chapterOfId(anyLong());
        verify(courseProvider, times(1)).courseOfId(anyLong());
    }

    @Test
    void save_throws_ResourceNotFoundException_if_chapter_not_found() {
        //given
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockUser(false)));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.update(new Lesson(), 100));

    }

    @Test
    void save_throws_ForbiddenActionException_if_user_is_not_admin_nor_course_author() {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(false);
        User author = new User();
        author.setId(user.getId() + 4);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList())));

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.update(new Lesson(), user.getId()));
    }

    @Test
    void save_throws_ResourceNotFound_if_lesson_not_found() {
        //given
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.update(new Lesson(), 100));
    }

    @Test
    void save_worksProperly_if_user_is_admin_and_not_author() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(true);
        User author = new User();
        author.setId(user.getId() + 4);

        Lesson existingLesson = new Lesson();
        existingLesson.setId(12L);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(existingLesson));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList())));

        when(lessonProvider.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Lesson updateLesson = new Lesson();
        updateLesson.setTitle(TITLE_TO_SAVE);
        updateLesson.setChapterId(561);
        updateLesson.setContent(CONTENT_TO_SAVE);
        updateLesson.setThumbnail(THUMBNAIL_TO_SAVE);
        updateLesson.setFree(true);
        updateLesson.setSummary(SUMMARY_TO_SAVE);
        updateLesson.setVideo(VIDEO_TO_SAVE);

        //when
        lesson = lessonService.update(updateLesson, user.getId());

        //then
        assertThat(lesson.getId()).isEqualTo(existingLesson.getId());
        assertThat(lesson.getTitle()).isEqualTo(TITLE_TO_SAVE);
        assertThat(lesson.getContent()).isEqualTo(CONTENT_TO_SAVE);
        assertThat(lesson.getType()).isEqualTo("video");
        assertThat(lesson.getThumbnail()).isEqualTo(THUMBNAIL_TO_SAVE);
        assertThat(lesson.isFree()).isTrue();
        assertThat(lesson.getSummary()).isEqualTo(SUMMARY_TO_SAVE);
        assertThat(lesson.getVideo()).isEqualTo(VIDEO_TO_SAVE);
        assertThat(lesson.getChapterId()).isEqualTo(existingLesson.getChapterId());

        verify(chapterProvider).chapterOfId(anyLong());
        verify(courseProvider).courseOfId(anyLong());
    }
/*
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
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.delete(100));
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
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.delete(100));
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
        lessonService.delete(100);

        //then
        assertThat(lessonProvider.deleteCalled).isTrue();
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
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.delete(999));
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
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.get(999))
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
        assertThatExceptionOfType(ResourceNotFoundException.class)
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
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.get(0))
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
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.get(12));
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
        Lesson result = lessonService.get(12);

        //then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getTitle()).isEqualTo("Lesson 1");
        assertThat(result.getChapterId()).isEqualTo(1);

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
        Lesson result = lessonService.get(0);

        //then
        assertThat(result.getId()).isEqualTo(20);
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(CONTENT);
        assertThat(result.getVideo()).isEqualTo(VIDEO_NOT_AVAILABLE_FOR_FREE);

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
        Lesson result = lessonService.get(0);

        //then
        assertThat(result.getId()).isEqualTo(20);
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(CONTENT);
        assertThat(result.getVideo()).isEqualTo(VIDEO_NOT_AVAILABLE_FOR_FREE);

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
        Lesson result = lessonService.get(5);

        //then
        assertThat(result.getId()).isEqualTo(20);
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(CONTENT);
        assertThat(result.getVideo()).isEqualTo(VIDEO);

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
        Lesson result = lessonService.get(0);

        //then
        assertThat(result.getId()).isEqualTo(20);
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(CONTENT);
        assertThat(result.getVideo()).isEqualTo(VIDEO);

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
        Lesson result = lessonService.get(85);

        //then
        assertThat(result.getId()).isEqualTo(20);
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(CONTENT);
        assertThat(result.getVideo()).isEqualTo(VIDEO);

    }
*/
}