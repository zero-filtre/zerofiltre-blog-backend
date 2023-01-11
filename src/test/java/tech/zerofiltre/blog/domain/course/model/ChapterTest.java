package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.doubles.*;

class ChapterTest {

    @Test
    void init_throws_UserNotFoundException_if_author_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .userProvider(new NotFoundUserProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> chapter.init("Chapter 1", 1, 100));
    }

    @Test
    void save_throws_UserNotFoundException_if_author_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .userProvider(new NotFoundUserProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> chapter.save(100));
    }

    @Test
    void save_throws_ResourceNotFoundException_if_course_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(new NotFoundCourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.save(100));
    }

    @Test
    void init_throws_ResourceNotFoundException_if_course_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(new NotFoundCourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.init("Chapter 1", 1, 100));
    }

    @Test
    void init_throws_ForbiddenActionException_if_user_is_not_admin_nor_course_author() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.init("Chapter 1", 1, 100));
    }

    @Test
    void save_throws_ForbiddenActionException_if_user_is_not_admin() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.save(100));
    }

    @Test
    void init_worksProperly_if_user_is_admin() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(new ChapterProviderSpy())
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        chapter.init("Chapter 1", 1, 100);

        //then
        Assertions.assertThat(chapter.getTitle()).isEqualTo("Chapter 1");
        Assertions.assertThat(chapter.getCourseId()).isEqualTo(1);
        Assertions.assertThat(chapter.getLessons()).isNotNull();
        Assertions.assertThat(chapter.getLessons()).isEmpty();
    }

    @Test
    void save_throws_ResourceNotFound_if_chapter_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(new ChapterProviderSpy())
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.save(100));
    }

    @Test
    void save_worksProperly_if_user_is_admin() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(chapterProvider)
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        chapter.save(100);

        //then
        Assertions.assertThat(chapter.getTitle()).isEqualTo("Chapter 1");
        Assertions.assertThat(chapter.getCourseId()).isEqualTo(1);
        Assertions.assertThat(chapter.getLessons()).isNotNull();
        Assertions.assertThat(chapter.getLessons()).isEmpty();
        Assertions.assertThat(chapterProvider.saveCalled).isTrue();
    }

    @Test
    void delete_throws_ResourceNotFound_if_chapter_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(new ChapterProviderSpy())
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.delete(100));
    }

    @Test
    void delete_throws_ForbiddenActionException_if_user_is_not_author() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.delete(100));
    }

    @Test
    void delete_works_if_user_is_not_author_but_is_admin() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(chapterProvider)
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        chapter.delete(100);

        //then
        Assertions.assertThat(chapterProvider.deleteCalled).isTrue();
    }

    @Test
    void delete_throws_ForbiddenActionException_if_chapter_contains_lessons() {
        //given
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(new FoundChapterWithLessonsProviderSpy())
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .title("Chapter 1")
                .courseId(1)
                .build();
        chapter.getLessons().add(Lesson.builder().build());

        //when
        //then
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.delete(100));
    }

    @Test
    void get_throws_ResourceNotFound_if_chapter_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .chapterProvider(new ChapterProviderSpy())
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.get(100));
    }

    @Test
    void get_returns_chapter_with_all_data() throws ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .chapterProvider(new FoundChapterWithLessonsProviderSpy())
                .build();

        //when
        Chapter result = chapter.get(100);

        //then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(1);
        Assertions.assertThat(result.getTitle()).isEqualTo("Chapter 1");
        Assertions.assertThat(result.getCourseId()).isEqualTo(1);
        Assertions.assertThat(result.getLessons()).isNotNull();
        Assertions.assertThat(result.getLessons()).isNotEmpty();
    }
}