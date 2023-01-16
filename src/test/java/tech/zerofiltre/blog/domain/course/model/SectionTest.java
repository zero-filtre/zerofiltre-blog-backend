package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class SectionTest {

    private SectionProvider sectionProvider;
    private Section section;


    @Test
    void save_isOK() {
        sectionProvider = new SectionProviderSpy();
        section = ZerofiltreUtils.createMockSections(sectionProvider, false).get(0);

        section = section.save();

        assertThat(((SectionProviderSpy) sectionProvider).saveCalled).isTrue();
    }

    @Test
    void findById_isOK() throws ResourceNotFoundException {
        sectionProvider = new FoundSectionProviderSpy();
        section = ZerofiltreUtils.createMockSections(sectionProvider, true).get(0);

        section = section.findById(45);

        assertThat(((FoundSectionProviderSpy) sectionProvider).findByIdCalled).isTrue();
    }

    @Test
    void findById_ThrowsResourceNotFoundException_whenSectionNotFound() {
        sectionProvider = new SectionProviderSpy();
        section = ZerofiltreUtils.createMockSections(sectionProvider, true).get(0);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> section.findById(45));
    }

    @Test
    void delete_isOK_ifUserNotNull() throws ForbiddenActionException, ResourceNotFoundException {
        sectionProvider = new FoundSectionProviderSpy();
        section = ZerofiltreUtils.createMockSections(sectionProvider, true).get(0);
        User user = ZerofiltreUtils.createMockUser(true);
        section.delete(user);

        assertThat(((FoundSectionProviderSpy) sectionProvider).deleteCalled).isTrue();
    }

    @Test
    void delete_ThrowsForbiddenActionExceptionIfDeleterIsNull() {
        sectionProvider = new FoundSectionProviderSpy();
        section = ZerofiltreUtils.createMockSections(sectionProvider, true).get(0);
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
        User user = ZerofiltreUtils.createMockUser(false);
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> section.delete(user));

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
        User user = ZerofiltreUtils.createMockUser(false);
        section.delete(user);
        assertThat(((FoundSectionProviderSpy) sectionProvider).deleteCalled).isTrue();


    }
}