package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.doubles.*;

class LessonTest {

    public static final String CONTENT_TO_SAVE = "content to save";
    public static final String TITLE_TO_SAVE = "Lesson 8";
    public static final String TYPE_TO_SAVE = "any";
    public static final String THUMBNAIL_TO_SAVE = "thumbnail";
    public static final String SUMMARY_TO_SAVE = "summary";
    public static final String VIDEO_TO_SAVE = "video";

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
        org.assertj.core.api.Assertions.assertThat(chapterProvider.chapterOfIdCalled).isTrue();
        org.assertj.core.api.Assertions.assertThat(courseProvider.courseOfIdCalled).isTrue();
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
                .type(TYPE_TO_SAVE)
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
        org.assertj.core.api.Assertions.assertThat(saved.getType()).isEqualTo(TYPE_TO_SAVE);
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
                .isThrownBy(() -> lesson.get());
    }

    @Test
    void get_returns_lesson_with_all_data() throws ResourceNotFoundException {
        //given
        Lesson lesson = Lesson.builder()
                .id(1)
                .userProvider(new FoundAdminUserProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .lessonProvider(new FoundLessonProviderSpy())
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .build();

        //when
        Lesson result = lesson.get();

        //then
        org.assertj.core.api.Assertions.assertThat(result.getId()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(result.getTitle()).isEqualTo("Lesson 1");
        org.assertj.core.api.Assertions.assertThat(result.getChapterId()).isEqualTo(1);

    }

}