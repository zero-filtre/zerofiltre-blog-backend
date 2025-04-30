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
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;
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
import static tech.zerofiltre.blog.domain.article.features.FindArticle.DOTS;
import static tech.zerofiltre.blog.domain.course.features.lesson.LessonService.THE_LESSON_OF_ID;
import static tech.zerofiltre.blog.domain.course.model.Chapter.DOES_NOT_EXIST;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.VIDEO_NOT_AVAILABLE_FOR_FREE;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

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

    @Mock
    DataChecker checker;

    @BeforeEach
    void setup() {
        lessonService = new LessonService(lessonProvider, chapterProvider, userProvider, courseProvider, enrollmentProvider, checker);
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
    void update_throws_ResourceNotFoundException_if_chapter_not_found() {
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
    void update_throws_ForbiddenActionException_if_user_is_not_admin_nor_course_author() {
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
                .isThrownBy(() -> lessonService.update(new Lesson(), user.getId()))
                .withMessage(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
    }

    @Test
    void update_throws_ForbiddenActionException_if_user_is_not_admin_nor_course_author_and__course_is_published() {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(false);
        User author = new User();
        author.setId(user.getId() + 4);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList())));

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.update(new Lesson(), user.getId()))
                .withMessage("You are not allowed to do this action on this course");
    }

    @Test
    void update_throws_ResourceNotFound_if_lesson_not_found() {
        //given
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.update(new Lesson(), 100));
    }

    @Test
    void update_worksProperly_if_user_is_admin_and_not_author() throws ResourceNotFoundException, ForbiddenActionException {
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

    @Test
    void getAsUser_Throws_ResourceNotFound_if_lesson_not_found() {
        //given
        long lessonId = 10L;

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.getAsUser(lessonId, 0))
                .withMessageContaining(THE_LESSON_OF_ID + lessonId + DOES_NOT_EXIST);
    }

    @Test
    void getAsUser_Throws_ResourceNotFound_if_chapter_not_found_for_not_connected_user() {
        //given
        lesson = new Lesson();
        lesson.setChapterId(10);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.getAsUser(0, 0))
                .withMessageContaining("The chapter with id: " + 10 + DOES_NOT_EXIST);
    }

    @Test
    void getAsUser_throws_ForbiddenActionException_if_not_connected_and_course_not_published() {
        //given
        long courseId = 4L;
        Chapter chapter = Chapter.builder().courseId(courseId).build();
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));
        when(courseProvider.courseOfId(courseId)).thenReturn(Optional.of(course));

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.getAsUser(0, 0))
                .withMessage(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
    }

    @Test
    void getAsUser_throws_ResourceNotFound_if_lesson_not_found() {
        //given
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.getAsUser(12, 0));
    }

    @Test
    void getAsUser_returns_lesson_with_all_data() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        long lessonId = 12L;
        long chapterId = 4L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapterId(chapterId);
        lesson.setTitle("Lesson 1");

        User user = new User();
        user.setId(3L);
        user.getRoles().add("ROLE_ADMIN");

        Chapter chapter = Chapter.builder().id(chapterId).build();

        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(chapterId)).thenReturn(Optional.of(chapter));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //when
        Lesson result = lessonService.getAsUser(lessonId, user.getId());

        //then
        assertThat(result.getId()).isEqualTo(lesson.getId());
        assertThat(result.getTitle()).isEqualTo(lesson.getTitle());
        assertThat(result.getChapterId()).isEqualTo(lesson.getChapterId());
    }

    @Test
    void getAsUser_returns_lesson_with_all_data_for_connected_user() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        long lessonId = 12L;
        long chapterId = 4L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapterId(chapterId);
        lesson.setTitle("Lesson 1");

        User user = new User();
        user.setId(3L);

        User author = new User();
        author.setId(8L);

        Chapter chapter = Chapter.builder().id(chapterId).build();

        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(chapterId)).thenReturn(Optional.of(chapter));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), eq(true))).thenReturn(Optional.of(new Enrollment()));

        //when
        Lesson result = lessonService.getAsUser(lessonId, user.getId());

        //then
        assertThat(result.getId()).isEqualTo(lesson.getId());
        assertThat(result.getTitle()).isEqualTo(lesson.getTitle());
        assertThat(result.getChapterId()).isEqualTo(lesson.getChapterId());
    }

    @Test
    void getAsUser_NonFreeLesson_returns_partOfContent_exceptVideo_ifNotPartOfEnrollment() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        long lessonId = 20L;
        long chapterId = 10L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapterId(chapterId);
        lesson.setTitle("Lesson 1");
        lesson.setFree(false);
        lesson.setContent(CONTENT + CONTENT + CONTENT + CONTENT);
        lesson.setVideo(VIDEO);

        User user = new User();
        user.setId(999);

        long courseId = 18L;
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course.setId(courseId);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(chapterId)).thenReturn(Optional.of(Chapter.builder().id(chapterId).courseId(courseId).build()));
        when(courseProvider.courseOfId(courseId)).thenReturn(Optional.of(course));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //when
        Lesson result = lessonService.getAsUser(lessonId, user.getId());

        //then
        assertThat(result.getId()).isEqualTo(lesson.getId());
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo( CONTENT + DOTS);
        assertThat(result.getVideo()).isEqualTo(VIDEO_NOT_AVAILABLE_FOR_FREE);
    }

    @Test
    void getAsUser_NonFreeLesson_returns_partOfContent_exceptVideo_ifNotConnected() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        long lessonId = 20L;
        long chapterId = 10L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapterId(chapterId);
        lesson.setTitle("Lesson 1");
        lesson.setFree(false);
        lesson.setContent(CONTENT + CONTENT + CONTENT + CONTENT);
        lesson.setVideo(VIDEO);

        long courseId = 18L;
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course.setId(courseId);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(chapterProvider.chapterOfId(chapterId)).thenReturn(Optional.of(Chapter.builder().id(chapterId).courseId(courseId).build()));
        when(courseProvider.courseOfId(courseId)).thenReturn(Optional.of(course));

        //when
        Lesson result = lessonService.getAsUser(lessonId, 0);

        //then
        assertThat(result.getId()).isEqualTo(lesson.getId());
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(CONTENT + DOTS);
        assertThat(result.getVideo()).isEqualTo(VIDEO_NOT_AVAILABLE_FOR_FREE);
    }

    @Test
    void getAsUser_freeLesson_returns_fullContent_evenNotPartOfEnrollment() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        long lessonId = 20L;
        long chapterId = 10L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapterId(chapterId);
        lesson.setTitle("Lesson 1");
        lesson.setFree(true);
        lesson.setContent(CONTENT);
        lesson.setVideo(VIDEO);

        User user = new User();
        user.setId(999);

        long courseId = 18L;
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course.setId(courseId);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(chapterId)).thenReturn(Optional.of(Chapter.builder().id(chapterId).courseId(courseId).build()));
        when(courseProvider.courseOfId(courseId)).thenReturn(Optional.of(course));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //when
        Lesson result = lessonService.getAsUser(lessonId, user.getId());

        //then
        assertThat(result.getId()).isEqualTo(lesson.getId());
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(lesson.getContent());
        assertThat(result.getVideo()).isEqualTo(lesson.getVideo());
    }

    @Test
    void getAsUser_freeLesson_returns_fullContent_evenNotConnected() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        long lessonId = 20L;
        long chapterId = 10L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapterId(chapterId);
        lesson.setTitle("Lesson 1");
        lesson.setFree(true);
        lesson.setContent(CONTENT);
        lesson.setVideo(VIDEO);

        long courseId = 18L;
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course.setId(courseId);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(chapterProvider.chapterOfId(chapterId)).thenReturn(Optional.of(Chapter.builder().id(chapterId).courseId(courseId).build()));
        when(courseProvider.courseOfId(courseId)).thenReturn(Optional.of(course));

        //when
        Lesson result = lessonService.getAsUser(lessonId, 0);

        //then
        assertThat(result.getId()).isEqualTo(lesson.getId());
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(lesson.getContent());
        assertThat(result.getVideo()).isEqualTo(lesson.getVideo());
    }

    @Test
    void getAsUser_Lesson_returns_fullContent_evenNotPartOfEnrollment_andAdmin() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        long lessonId = 20L;
        long chapterId = 10L;
        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapterId(chapterId);
        lesson.setTitle("Lesson 1");
        lesson.setFree(true);
        lesson.setContent(CONTENT);
        lesson.setVideo(VIDEO);

        User user = new User();
        user.setId(999);
        user.getRoles().add("ROLE_ADMIN");

        long courseId = 18L;
        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course.setId(courseId);

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(lesson));
        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(chapterId)).thenReturn(Optional.of(Chapter.builder().id(chapterId).courseId(courseId).build()));
        when(courseProvider.courseOfId(courseId)).thenReturn(Optional.of(course));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //when
        Lesson result = lessonService.getAsUser(lessonId, user.getId());

        //then
        assertThat(result.getId()).isEqualTo(lesson.getId());
        assertThat(result.getType()).isEqualTo("video");
        assertThat(result.getContent()).isEqualTo(lesson.getContent());
        assertThat(result.getVideo()).isEqualTo(lesson.getVideo());
    }

    @Test
    void getAsUser_throws_ForbiddenActionException_if_not_admin_nor_author_and_course_not_published() {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(7L);

        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.getAsUser(4L, user.getId()))
                .withMessage(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
    }

    @Test
    void delete_throws_ResourceNotFound_if_lesson_not_found() {
        //given
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.empty());

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> lessonService.delete(100, 0));
    }

    @Test
    void delete_throws_ForbiddenActionException_if_user_is_not_author_nor_admin() {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(4L);

        User author = ZerofiltreUtilsTest.createMockUser(false);
        author.setId(7L);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockUser(false)));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.delete(100, user.getId()))
                .withMessage(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
    }

    @Test
    void delete_throws_ForbiddenActionException_if_user_is_author_and_course_is_published() {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(4L);

        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockUser(false)));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> lessonService.delete(100, user.getId()))
                .withMessage("You can not delete a lesson that is already published");
    }

    @Test
    void delete_works_if_user_is_not_author_but_is_admin() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(true);
        user.setId(4L);

        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtilsTest.createMockUser(false)));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //when
        lessonService.delete(100, user.getId());

        //then
        verify(lessonProvider).delete(any(Lesson.class));
    }

    @Test
    void onlyAdminCanDeleteLesson_ofPublishedCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(true);
        user.setId(999);

        User author = ZerofiltreUtilsTest.createMockUser(false);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());

        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(new Lesson()));
        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().build()));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //when
        lessonService.delete(4L, user.getId());

        //then
        verify(lessonProvider).delete(any(Lesson.class));
    }

}