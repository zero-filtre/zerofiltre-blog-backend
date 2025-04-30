package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.course.model.Chapter.*;
import static tech.zerofiltre.blog.domain.course.model.Lesson.THE_LESSON_OF_ID;

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
    @DisplayName("When a platform admin initializes a chapter for a course owned by the company, the chapter is initialized.")
    void shouldInitChapter_whenUserIsAdmin_andCourseOwnedByCompany() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = mock(DataChecker.class);

        Chapter chapter = builder()
                .id(1)
                .chapterProvider(new ChapterProviderSpy())
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .checker(checker)
                .title("Chapter 1")
                .courseId(course.getId())
                .build();

        //WHEN
        chapter.init("Chapter 1", course.getId(), 100);

        //THEN
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).checkIfAdminOrCompanyAdminOrEditor(any(User.class), anyLong());

        assertThat(chapter.getTitle()).isEqualTo("Chapter 1");
        assertThat(chapter.getCourseId()).isEqualTo(course.getId());
        assertThat(chapter.getLessons()).isNotNull();
        Assertions.assertThat(chapter.getLessons()).isEmpty();
    }

    @Test
    void init_defines_chapter_number() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));


        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        when(chapterProvider.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Chapter firstChapter = Chapter.builder().id(1).number(1).build();
        Chapter secondChapter = Chapter.builder().id(2).number(2).build();
        when(chapterProvider.ofCourseId(anyLong())).thenReturn(Arrays.asList(firstChapter, secondChapter));


        UserProvider userProvider = mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .title("Chapter 1")
                .build()
                .init("Chapter 1", 1, 100);

        assertThat(chapter.getNumber()).isEqualTo(3);
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
    @DisplayName("When a non-company admin wants to delete a chapter of a published course owned by the company, a forbidden action exception is thrown.")
    void shouldThrowException_whenCompanyEditorOrViewerWantDeletePublishedChapterOfForCompanyCourse() {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = Mockito.mock(DataChecker.class);
        when(checker.isCompanyAdmin(anyLong(), anyLong())).thenReturn(false);

        Chapter chapter = builder()
                .id(1)
                .chapterProvider(new FoundChapterProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .checker(checker)
                .title("Chapter 1")
                .courseId(1)
                .build();

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.delete(100))
                .withMessage("You are not allowed to delete a chapter for a published course, please get in touch with an admin");

        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).isCompanyAdmin(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a company admin wants to delete a chapter of a published course owned by the company, the chapter is deleted.")
    void shouldDeleteChapter_whenCompanyAdminWantDeletePublishedChapterOfForCompanyCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = Mockito.mock(DataChecker.class);
        when(checker.isCompanyAdmin(anyLong(), anyLong())).thenReturn(true);

        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Chapter chapter = builder()
                .id(1)
                .chapterProvider(chapterProvider)
                .userProvider(new FoundNonAdminUserProviderSpy())
                .courseProvider(courseProvider)
                .checker(checker)
                .title("Chapter 1")
                .courseId(1)
                .build();

        //WHEN
        chapter.delete(100);

        //THEN
        assertThat(chapterProvider.deleteCalled).isTrue();
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).isCompanyAdmin(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a platform admin deletes a chapter from a course with published status, the chapter is deleted.")
    void shouldDeleteChapter_whenPlatformAdminDeleteChapterForPublishedCourse() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        FoundChapterProviderSpy chapterProvider = new FoundChapterProviderSpy();
        Chapter chapter = Chapter.builder()
                .id(1)
                .chapterProvider(chapterProvider)
                .userProvider(new FoundAdminUserProviderSpy())
                .courseProvider(new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons())
                .title("Chapter 1")
                .courseId(1)
                .build();

        //when
        chapter.delete(100);

        //then
        assertThat(chapterProvider.deleteCalled).isTrue();
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

        User author = ZerofiltreUtilsTest.createMockUser(false);
        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(author));

        ChapterProvider chapterProvider = mock(ChapterProvider.class);
        Mockito.when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(Chapter.builder().id(1).courseId(1).build()));

        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
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
        List<Chapter> result = chapter.getByCourseId(ZerofiltreUtilsTest.createMockUser(true));

        //then
        Assertions.assertThat(result).isNotEmpty().hasSize(2);
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
        List<Chapter> result = chapter.getByCourseId(ZerofiltreUtilsTest.createMockUser(true));

        //then
        Assertions.assertThat(result).isNotEmpty().hasSize(2);
    }

    @Test
    void getChaptersByCourseId_works_if_user_is_not_author_but_admin() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(ZerofiltreUtilsTest.createMockUser(true));

        //then
        Assertions.assertThat(result).isNotEmpty().hasSize(2);
    }

    @Test
    void getChaptersByCourseId_works_if_user_is_author() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Chapter chapter = Chapter.builder()
                .courseProvider(new Found_Draft_WithKnownAuthor_CourseProvider_Spy())
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(ZerofiltreUtilsTest.createMockUser(false));

        //then
        Assertions.assertThat(result).isNotEmpty().hasSize(2);
    }

    @Test
    void getChaptersByCourseId_works_if_user_is_null() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.empty());

        Chapter chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //when
        List<Chapter> result = chapter.getByCourseId(null);

        //then
        Assertions.assertThat(result).isNotEmpty().hasSize(2);
    }

    @Test
    @DisplayName("When a company user wants to view all chapters of a company-owned course with published status, the user is authorized.")
    void shouldFindAllCourseChapters_whenUserIsCompanyUserAndCompanyOwnedCourseWithStatusPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        DataChecker checker = mock(DataChecker.class);

        User mockUser = ZerofiltreUtilsTest.createMockUser(false);
        doNothing().when(checker).checkIfAdminOrCompanyUser(mockUser, 12L);

        Chapter chapter = builder()
                .courseProvider(courseProvider)
                .chapterProvider(new FoundChapterProviderSpy())
                .checker(checker)
                .build();

        //WHEN
        List<Chapter> result = chapter.getByCourseId(mockUser);

        //THEN
        assertThat(result).hasSize(2);
        verify(courseProvider).idOfCompanyOwningCourse(anyLong());
        verify(checker).checkIfAdminOrCompanyUser(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When a connected user wants to view all chapters of a non company-owned course with published status, the user is authorized.")
    void shouldFindAllCourseChapters_whenUserConnected_AndNonCompanyOwnedCourse_WithStatusPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.empty());

        DataChecker checker = mock(DataChecker.class);
        User mockUser = ZerofiltreUtilsTest.createMockUser(false);

        Chapter chapter = builder()
                .courseProvider(courseProvider)
                .chapterProvider(new FoundChapterProviderSpy())
                .checker(checker)
                .build();

        //WHEN
        List<Chapter> result = chapter.getByCourseId(mockUser);

        //THEN
        assertThat(result).hasSize(2);
        verify(checker,times(0)).checkIfAdminOrCompanyUser(any(User.class), anyLong());
    }

    @Test
    @DisplayName("When a non-authenticated user wants to view all chapters of a company course with published status, a forbidden action exception is thrown.")
    void shouldThrowException_whenUserIsNullAndCompanyCourseStatusIsPublished() {
        //given
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        when(courseProvider.idOfCompanyOwningCourse(anyLong())).thenReturn(Optional.of(12L));

        Chapter chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.getByCourseId(null))
                .withMessage("You are not allowed to get chapters for this course");
    }

    @Test
    @DisplayName("When a non-authenticated user wants to view all chapters of a course with draft status, a forbidden action exception is thrown.")
    void shouldThrowException_whenUserIsNullAndCourseStatusIsNotPublished() {
        //GIVEN
        CourseProvider courseProvider = mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .chapterProvider(new FoundChapterProviderSpy())
                .build();

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.getByCourseId(null))
                .withMessage("You are not allowed to get chapters for this course");
    }

    @Test
    void moveLessonDownIsOk() throws ResourceNotFoundException, ForbiddenActionException {
        LessonProvider lessonProvider = Mockito.mock(LessonProvider.class);
        when(lessonProvider.saveAll(anyList())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);

        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        Lesson firstLesson = Lesson.builder().id(1).number(1).build();
        Lesson secondLesson = Lesson.builder().id(2).number(2).build();
        Lesson thirdLesson = Lesson.builder().id(3).number(3).build();

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter chapter = Chapter.builder()
                .lessons(List.of(firstLesson, secondLesson, thirdLesson))
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .build();
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));


        Chapter result = chapter.moveLesson(5, 1, 2);
        List<Lesson> reorderedLessons = result.getLessons();

        // Check that the moved lesson is now in position 2
        reorderedLessons.forEach(lesson -> {
            if (lesson.getId() == 1) assertThat(lesson.getNumber()).isEqualTo(2);
            if (lesson.getId() == 2) assertThat(lesson.getNumber()).isEqualTo(1);
            if (lesson.getId() == 3) assertThat(lesson.getNumber()).isEqualTo(3);
        });
    }

    @Test
    void moveLessonUpIsOk() throws ResourceNotFoundException, ForbiddenActionException {
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        LessonProvider lessonProvider = Mockito.mock(LessonProvider.class);
        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Lesson firstLesson = Lesson.builder().id(1).number(1).build();
        Lesson secondLesson = Lesson.builder().id(2).number(2).build();
        Lesson thirdLesson = Lesson.builder().id(3).number(3).build();

        Chapter chapter = Chapter.builder()
                .lessons(List.of(firstLesson, secondLesson, thirdLesson))
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));
        when(lessonProvider.saveAll(anyList())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        Chapter result = chapter.moveLesson(5, 3, 2);
        List<Lesson> reorderedLessons = result.getLessons();

        // Check that the moved lesson is now in position 2
        reorderedLessons.forEach(lesson -> {
            if (lesson.getId() == 1) assertThat(lesson.getNumber()).isEqualTo(1);
            if (lesson.getId() == 2) assertThat(lesson.getNumber()).isEqualTo(3);
            if (lesson.getId() == 3) assertThat(lesson.getNumber()).isEqualTo(2);
        });
    }

    @Test
    void moveLessonToTheSamePosition() throws ResourceNotFoundException, ForbiddenActionException {
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        LessonProvider lessonProvider = Mockito.mock(LessonProvider.class);
        when(lessonProvider.saveAll(anyList())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Lesson firstLesson = Lesson.builder().id(1).number(1).build();
        Lesson secondLesson = Lesson.builder().id(2).number(2).build();
        Lesson thirdLesson = Lesson.builder().id(3).number(3).build();

        Chapter chapter = Chapter.builder()
                .lessons(List.of(firstLesson, secondLesson, thirdLesson))
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));


        Chapter result = chapter.moveLesson(5, 2, 2);
        List<Lesson> reorderedLessons = result.getLessons();

        // Check that the moved lesson is now in position 2
        reorderedLessons.forEach(lesson -> {
            if (lesson.getId() == 1) assertThat(lesson.getNumber()).isEqualTo(1);
            if (lesson.getId() == 2) assertThat(lesson.getNumber()).isEqualTo(2);
            if (lesson.getId() == 3) assertThat(lesson.getNumber()).isEqualTo(3);
        });
    }


    @Test
    void moveLessonDown_throws_ResourceNotFoundException_if_chapter_not_found() {

        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.empty());
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .build();

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.moveLesson(5, 1, 2))
                .withMessageContaining(THE_CHAPTER_WITH_ID);
    }

    @Test
    void moveLessonDown_throws_ResourceNotFoundException_if_lesson_not_found() {
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));
        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .lessons(Collections.emptyList())
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();

        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));


        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.moveLesson(5, 1, 2))
                .withMessageContaining(THE_LESSON_OF_ID);
    }


    @Test
    void moveLesson_throws_ForbiddenActionException_if_user_is_not_admin_norAuthor() {
        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);
        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        UserProvider userProvider = Mockito.mock(UserProvider.class);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(999);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(chapter));

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.moveLesson(5, 1, 2));
    }

    @Test
    void moveChapter_throws_ResourceNotFoundException_if_user_not_found() {
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());
        Chapter chapter = Chapter.builder()
                .userProvider(userProvider)
                .build();

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.move(5, 2))
                .withMessageContaining(USER_DOES_NOT_EXIST);
    }

    @Test
    void moveChapter_throws_ResourceNotFoundException_if_course_not_found() {

        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.empty());

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();

        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(chapter));


        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.move(5, 2))
                .withMessageContaining(THE_COURSE_WITH_ID);
    }

    @Test
    void moveChapter_throws_ResourceNotFoundException_if_chapter_not_found() {

        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.empty());
        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .build();

        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> chapter.move(5, 2))
                .withMessageContaining(THE_CHAPTER_WITH_ID);
    }

    @Test
    void moveChapter_throws_ForbiddenActionException_if_user_is_not_admin_norAuthor() {
        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);
        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        UserProvider userProvider = Mockito.mock(UserProvider.class);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setId(999);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter chapter = Chapter.builder()
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.ofNullable(chapter));

        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> chapter.move(5, 2))
                .withMessageContaining(YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE);
    }

    @Test
    void moveChapterUpIsOK() throws ZerofiltreException {
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter firstChapter = Chapter.builder().id(1).number(1).build();
        Chapter secondChapter = Chapter.builder().id(2).number(2).build();

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);


        Chapter chapter = Chapter.builder()
                .id(3)
                .number(3)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();

        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));
        when(chapterProvider.ofCourseId(anyLong())).thenReturn(List.of(firstChapter, secondChapter, chapter));
        when(chapterProvider.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));


        chapter.move(5, 2);
        List<Chapter> reorderedChapters = chapter.getByCourseId(user);

        // Check that the moved lesson is now in position 2
        reorderedChapters.forEach(aChapter -> {
            if (aChapter.getId() == 1) assertThat(aChapter.getNumber()).isEqualTo(1);
            if (aChapter.getId() == 2) assertThat(aChapter.getNumber()).isEqualTo(3);
            if (aChapter.getId() == 3) assertThat(aChapter.getNumber()).isEqualTo(2);
        });
    }

    @Test
    void moveChapterToTheSamePosition_isOK() throws ZerofiltreException {
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter firstChapter = Chapter.builder().id(1).number(1).build();
        Chapter secondChapter = Chapter.builder().id(2).number(2).build();

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);


        Chapter chapter = Chapter.builder()
                .id(3)
                .number(3)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();

        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));
        when(chapterProvider.ofCourseId(anyLong())).thenReturn(List.of(firstChapter, secondChapter, chapter));
        when(chapterProvider.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));


        chapter.move(5, 3);
        List<Chapter> reorderedChapters = chapter.getByCourseId(user);

        // Check that the moved lesson is now in position 2
        reorderedChapters.forEach(aChapter -> {
            if (aChapter.getId() == 1) assertThat(aChapter.getNumber()).isEqualTo(1);
            if (aChapter.getId() == 2) assertThat(aChapter.getNumber()).isEqualTo(2);
            if (aChapter.getId() == 3) assertThat(aChapter.getNumber()).isEqualTo(3);
        });
    }

    @Test
    void moveChapterDown_isOk() throws ZerofiltreException {
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        when(userProvider.userOfId(5)).thenReturn(Optional.of(user));

        CourseProvider courseProvider = Mockito.mock(CourseProvider.class);
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        Chapter firstChapter = Chapter.builder().id(1).number(1).build();
        Chapter secondChapter = Chapter.builder().id(3).number(3).build();

        ChapterProvider chapterProvider = Mockito.mock(ChapterProvider.class);


        Chapter chapter = Chapter.builder()
                .id(2)
                .number(2)
                .chapterProvider(chapterProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build();

        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(chapter));
        when(chapterProvider.ofCourseId(anyLong())).thenReturn(List.of(firstChapter, secondChapter, chapter));
        when(chapterProvider.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));


        chapter.move(5, 3);
        List<Chapter> reorderedChapters = chapter.getByCourseId(user);

        // Check that the moved lesson is now in position 2
        reorderedChapters.forEach(aChapter -> {
            if (aChapter.getId() == 1) assertThat(aChapter.getNumber()).isEqualTo(1);
            if (aChapter.getId() == 2) assertThat(aChapter.getNumber()).isEqualTo(3);
            if (aChapter.getId() == 3) assertThat(aChapter.getNumber()).isEqualTo(2);
        });
    }

}