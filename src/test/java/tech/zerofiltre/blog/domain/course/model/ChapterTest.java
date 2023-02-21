package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    void delete_throws_ForbiddenActionException_if_chapter_course_isPublished_andAuthorNotAdmin() {

        User author = ZerofiltreUtils.createMockUser(false);
        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(author));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        Mockito.when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(1).courseId(1).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        Mockito.when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //given
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();

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
                .isThrownBy(chapter::get);
    }

    @Test
    void get_returns_chapter_with_all_data() throws ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .chapterProvider(new FoundChapterWithLessonsProviderSpy())
                .build();

        //when
        Chapter result = chapter.get();

        //then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(1);
        Assertions.assertThat(result.getTitle()).isEqualTo("Chapter 1");
        Assertions.assertThat(result.getCourseId()).isEqualTo(1);
        Assertions.assertThat(result.getLessons()).isNotNull();
        Assertions.assertThat(result.getLessons()).isNotEmpty();
    }

    @Test
    void getChaptersByCourseId_returns_all_chapters_for_course() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        Chapter chapter = Chapter.builder()
                .chapterProvider(new FoundChapterProviderSpy())
                .courseProvider(new Found_Published_WithUnknownAuthor_CourseProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(new User());

        //then
        Assertions.assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void getChaptersByCourseId_throws_ResourceNotFound_if_course_not_found() {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new NotFoundCourseProviderSpy())
                .chapterProvider(new ChapterProviderSpy())
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.getByCourseId(new User()));
    }

    @Test
    void getChaptersByCourseId_throws_ForbiddenActionException_if_user_is_not_author_and_course_not_published() {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .chapterProvider(new ChapterProviderSpy())
                .build();

        //when
        //then
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.getByCourseId(new User()));
    }

    @Test
    void getChaptersByCourseId_works_if_course_is_published() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new Found_Published_WithUnknownAuthor_CourseProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(new User());

        //then
        Assertions.assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void getChaptersByCourseId_works_if_user_is_not_author_but_admin() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(ZerofiltreUtils.createMockUser(true));

        //then
        Assertions.assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void getChaptersByCourseId_works_if_user_is_author() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(ZerofiltreUtils.createMockUser(false));

        //then
        Assertions.assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void getChaptersByCourseId_works_if_user_is_null() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new Found_Published_WithUnknownAuthor_CourseProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(null);

        //then
        Assertions.assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
    }
}