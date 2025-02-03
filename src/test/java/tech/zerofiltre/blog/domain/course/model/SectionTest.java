package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.*;

class SectionTest {

    public static final String UPDATED_TITLE = "updated title";
    public static final String UPDATED_CONTENT = "updated content";
    public static final String UPDATED_IMAGE = "updated image";
    private SectionProvider sectionProvider;
    private Section section;


    @Test
    void init_isOK() throws ForbiddenActionException, ResourceNotFoundException {
        sectionProvider = new SectionProviderSpy();
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        CourseProvider courseProvider = mock(CourseProvider.class);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        section = ZerofiltreUtilsTest.createMockSections(sectionProvider, courseProvider, false).get(0);
        User user = ZerofiltreUtilsTest.createMockUser(true);


        section = section.init(user);

        assertThat(((SectionProviderSpy) sectionProvider).saveCalled).isTrue();
    }

    @Test
    void update_UpdatesFieldsProperly() throws ZerofiltreException {
        SectionProvider mockSectionProvider = mock(SectionProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);

        User user = ZerofiltreUtilsTest.createMockUser(true);

        section = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_1")
                .content("TEST_SECTION_CONTENT_1")
                .id(1)
                .image("TEST_THUMBNAIL")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(1)
                .build();

        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.DRAFT, user, List.of(section), Collections.emptyList());
        when(mockSectionProvider.findById(anyLong())).thenReturn(Optional.ofNullable(section));
        when(mockSectionProvider.save(any())).thenReturn(section);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        section = section.update(section.getId(), UPDATED_TITLE, UPDATED_CONTENT, UPDATED_IMAGE, 999, user);

        assertThat(section.getId()).isEqualTo(1);
        assertThat(section.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(section.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(section.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(section.getPosition()).isEqualTo(999);
    }

    @Test
    void update_ThrowsResourceNotFoundException_ifCourseNotFound() {
        SectionProvider mockSectionProvider = mock(SectionProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);

        User user = ZerofiltreUtilsTest.createMockUser(true);

        section = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_1")
                .content("TEST_SECTION_CONTENT_1")
                .id(1)
                .image("TEST_THUMBNAIL")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(1)
                .build();

        when(mockSectionProvider.findById(anyLong())).thenReturn(Optional.ofNullable(section));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> section.update(section.getId(), UPDATED_TITLE, UPDATED_CONTENT, UPDATED_IMAGE, 999, user))
                .withMessageContaining(THE_COURSE_WITH_ID);

    }

    @Test
    void update_reordersSections_IfPositionIsChanged() throws ZerofiltreException {
        SectionProvider mockSectionProvider = mock(SectionProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);

        User user = ZerofiltreUtilsTest.createMockUser(true);

        section = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_1")
                .content("TEST_SECTION_CONTENT_1")
                .id(1)
                .image("TEST_THUMBNAIL")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(1)
                .build();

        Section section2 = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_2")
                .content("TEST_SECTION_CONTENT_2")
                .id(2)
                .image("TEST_THUMBNAIL2")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(2)
                .build();

        Section section3 = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_3")
                .content("TEST_SECTION_CONTENT_3")
                .id(3)
                .image("TEST_THUMBNAIL3")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(3)
                .build();


        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.DRAFT, user, List.of(section, section2, section3), Collections.emptyList());
        when(mockSectionProvider.findById(anyLong())).thenReturn(Optional.ofNullable(section));
        when(mockSectionProvider.save(any())).thenReturn(section);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        section = section.update(section.getId(), UPDATED_TITLE, UPDATED_CONTENT, UPDATED_IMAGE, 2, user);

        assertThat(section.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(section.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(section.getImage()).isEqualTo(UPDATED_IMAGE);

        verify(mockSectionProvider, times(1)).save(argThat(aSection -> aSection.getId() == 1 && aSection.getPosition() == 2));
        verify(mockSectionProvider, times(1)).save(argThat(aSection -> aSection.getId() == 2 && aSection.getPosition() == 1));
        verify(mockSectionProvider, times(1)).save(argThat(aSection -> aSection.getId() == 3 && aSection.getPosition() == 3));

        assertThat(section.getPosition()).isEqualTo(2);

    }

    @Test
    void update_DoesNotReorderSections_IfPositionIsSame() throws ZerofiltreException {
        SectionProvider mockSectionProvider = mock(SectionProvider.class);
        CourseProvider courseProvider = mock(CourseProvider.class);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        section = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_1")
                .content("TEST_SECTION_CONTENT_1")
                .id(1)
                .image("TEST_THUMBNAIL")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(1)
                .build();

        Section section2 = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_2")
                .content("TEST_SECTION_CONTENT_2")
                .id(2)
                .image("TEST_THUMBNAIL2")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(2)
                .build();

        Section section3 = new Section.SectionBuilder()
                .title("TEST_SECTION_TITLE_3")
                .content("TEST_SECTION_CONTENT_3")
                .id(3)
                .image("TEST_THUMBNAIL3")
                .sectionProvider(mockSectionProvider)
                .courseProvider(courseProvider)
                .position(3)
                .build();


        Course course = ZerofiltreUtilsTest.createMockCourse(true, Status.DRAFT, user, List.of(section, section2, section3), Collections.emptyList());
        when(mockSectionProvider.findById(anyLong())).thenReturn(Optional.ofNullable(section));
        when(mockSectionProvider.save(any())).thenReturn(section);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        section = section.update(section.getId(), UPDATED_TITLE, UPDATED_CONTENT, UPDATED_IMAGE, 1, user);

        assertThat(section.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(section.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(section.getImage()).isEqualTo(UPDATED_IMAGE);

        verify(mockSectionProvider, times(1)).save(any());
        assertThat(section.getPosition()).isEqualTo(1);

    }


    @Test
    void findById_isOK() throws ResourceNotFoundException {
        sectionProvider = new FoundSectionProviderSpy();
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        CourseProvider courseProvider = mock(CourseProvider.class);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        section = ZerofiltreUtilsTest.createMockSections(sectionProvider, courseProvider, true).get(0);

        section = section.findById(45);

        assertThat(((FoundSectionProviderSpy) sectionProvider).findByIdCalled).isTrue();
    }

    @Test
    void findById_ThrowsResourceNotFoundException_whenSectionNotFound() {
        sectionProvider = new SectionProviderSpy();
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        CourseProvider courseProvider = mock(CourseProvider.class);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        section = ZerofiltreUtilsTest.createMockSections(sectionProvider, courseProvider, true).get(0);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> section.findById(45));
    }

    @Test
    void delete_isOK_ifUserNotNull() throws ForbiddenActionException, ResourceNotFoundException {
        sectionProvider = new FoundSectionProviderSpy();
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        CourseProvider courseProvider = mock(CourseProvider.class);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        section = ZerofiltreUtilsTest.createMockSections(sectionProvider, courseProvider, true).get(0);
        User user = ZerofiltreUtilsTest.createMockUser(true);
        section.delete(user);

        assertThat(((FoundSectionProviderSpy) sectionProvider).deleteCalled).isTrue();
    }

    @Test
    void delete_ThrowsForbiddenActionExceptionIfDeleterIsNull() {
        sectionProvider = new FoundSectionProviderSpy();
        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        CourseProvider courseProvider = mock(CourseProvider.class);

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));
        section = ZerofiltreUtilsTest.createMockSections(sectionProvider, courseProvider, true).get(0);
        User user = null;
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> section.delete(user));

    }

    @Test
    void delete_ThrowsForbiddenActionException_IfDeleterIsNotAdminNorAuthor() {
        sectionProvider = new FoundSectionProviderSpy();
        section = Section.builder()
                .sectionProvider(sectionProvider)
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .loggerProvider(new Slf4jLoggerProvider())
                .build();
        User user = ZerofiltreUtilsTest.createMockUser(false);
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> section.delete(user));
    }

    @Test
    void delete_ThrowsResourceNotFoundException_IfSectionNotFound() {
        sectionProvider = new SectionProviderSpy();
        section = Section.builder()
                .id(45)
                .sectionProvider(sectionProvider)
                .chapterProvider(new FoundChapterProviderSpy())
                .courseProvider(new Found_Published_WithUnknownAuthor_CourseProviderSpy())
                .userProvider(new FoundNonAdminUserProviderSpy())
                .loggerProvider(new Slf4jLoggerProvider())
                .build();
        User user = ZerofiltreUtilsTest.createMockUser(false);
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> section.delete(user))
                .withMessage("The section with id: 45 does not exist");
    }

    @Test
    void delete_isOK_IfDeleterIsAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        sectionProvider = new FoundSectionProviderSpy();
        section = Section.builder()
                .sectionProvider(sectionProvider)
                .courseProvider(new Found_Draft_WithUnknownAuthor_CourseProviderSpy())
                .userProvider(new FoundAdminUserProviderSpy())
                .loggerProvider(new Slf4jLoggerProvider())
                .build();
        User user = ZerofiltreUtilsTest.createMockUser(true);
        section.delete(user);
        assertThat(((FoundSectionProviderSpy) sectionProvider).deleteCalled).isTrue();
    }
}