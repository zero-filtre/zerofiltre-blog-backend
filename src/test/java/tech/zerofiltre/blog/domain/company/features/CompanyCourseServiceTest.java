package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyCourseServiceTest {

    private static User adminUser;
    private static User userWithRoleUser;

    private CompanyCourseService companyCourseService;

    @Mock
    CompanyCourseProvider companyCourseProvider;

    @Mock
    DataChecker checker;

    @BeforeAll
    static void setup() {
        adminUser = new User();
        adminUser.getRoles().add("ROLE_ADMIN");

        userWithRoleUser = new User();
        userWithRoleUser.getRoles().add("ROLE_USER");
    }

    @BeforeEach
    void init() {
        companyCourseService = new CompanyCourseService(companyCourseProvider, checker);
    }

    @Test
    @DisplayName("When link a course to a company as an admin user, then verify that the link is saved with the correct parameters")
    void saveAsAdminUser_linkACourseToCompany_withCorrectParameters() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.link(adminUser, 1L, 1L);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When link the course to the company and the link already exists, then return that link")
    void whenLink_alreadyExistsLink_returnLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("As an admin, when link a suspended link, then verify that the link is saved with the correct parameters")
    void save_asAdminUser_linkASuspendedLink_withCorrectParameters() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.link(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId());

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.getCompanyId()).isEqualTo(linkCompanyCourse.getCompanyId());
        assertThat(linkCompanyCourseCaptured.getCourseId()).isEqualTo(linkCompanyCourse.getCourseId());
        assertThat(linkCompanyCourseCaptured.isActive()).isTrue();
        assertThat(linkCompanyCourseCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When link a course to a company as an non-admin user, a forbidden action exception is returned.")
    void whenLink_asNonAdminUser_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.link(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When link a course to a company as an admin user and the company does not exist, a resource not found exception is returned.")
    void whenLink_asAdminUser_notExistingCompany_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When link a course to a company as an admin user and the course does not exist, a resource not found exception is returned.")
    void whenLink_asAdminUser_notExistingCourse_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.link(adminUser, 2L, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When active all links of a company as an admin user then verify that the links are saved with the correct parameters")
    void whenActiveAllByCompanyId_asAdminUser_thenSaveAllLinks_withCorrectParameters() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse1 = new LinkCompanyCourse(1L, 1L, 1L, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusWeeks(1));
        LinkCompanyCourse linkCompanyCourse2 = new LinkCompanyCourse(2L, 1L, 2L, false, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusWeeks(2));
        List<LinkCompanyCourse> list = new ArrayList<>();
        list.add(linkCompanyCourse1);
        list.add(linkCompanyCourse2);

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(list);

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 1L);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider, times(2)).save(captor.capture());
        List<LinkCompanyCourse> listCaptured = captor.getAllValues();
        assertThat(listCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyCourse1.getCompanyId());
        assertThat(listCaptured.get(0).getCourseId()).isEqualTo(linkCompanyCourse1.getCourseId());
        assertThat(listCaptured.get(0).isActive()).isTrue();
        assertThat(listCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyCourse1.getLinkedAt());
        assertThat(listCaptured.get(0).getSuspendedAt()).isNull();

        assertThat(listCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyCourse2.getCompanyId());
        assertThat(listCaptured.get(1).getCourseId()).isEqualTo(linkCompanyCourse2.getCourseId());
        assertThat(listCaptured.get(1).isActive()).isTrue();
        assertThat(listCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyCourse2.getLinkedAt());
        assertThat(listCaptured.get(1).getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When active all links of a company as a non-admin user, a forbidden action exception is returned.")
    void whenActiveAllByCompanyId_asNotAdminUser_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(userWithRoleUser, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When active all links of a company as an admin user and the company does not exist, a resource not found exception is returned.")
    void whenActiveAllByCompanyId_asAdminUser_forNotExitingCompany_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.activeAllByCompanyId(adminUser, 2L));

        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When active all links of a company as an admin user and the links do not exist, then verify that the method to save is not called")
    void emptyListCompanyCourse_whenActiveAllByCompanyId_thenVerifyNotCallCompanyCourseProviderLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(new ArrayList<>());

        //WHEN
        companyCourseService.activeAllByCompanyId(adminUser, 2L);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When want to find a link between a course and a company as the platform or company admin, then verify that the method to find by the company id and course id is called")
    void whenFindLink_asPlatformOrCompanyAdmin_thenVerifyCallFindByCompanyIdAndCourseId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.find(adminUser, 1L, 1L);

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want to find a link between a course and a company as a non-admin user of the platform or the company, a forbidden action exception is returned.")
    void whenFindLink_asNonAdminUserPlatformOrCompany_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want to find a link between a course and a company and the company does not exist, a resource not found exception is returned.")
    void whenFindLinkForNonExistingCompany_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want to find a link between a course and a company and the course does not exist, a resource not found exception is returned.")
    void whenFindLinkForNonExistingCourse_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.courseExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.find(userWithRoleUser, 2L, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider, never()).findByCompanyIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a company then verify call find all by company id by page")
    void whenFindAllLinksByCompany_thenVerifyCallFindAllByCompanyIdByPage() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.findAllByCompanyId(adminUser, 0, 0, 1L);

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a company as a non-admin user of the platform or the company, a forbidden action exception is returned.")
    void whenFindAllLinksByCompany_asNonAdminUserOfPlatformOrCompany_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(companyCourseProvider, never()).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a company and the company does not exist, a resource not found exception is returned.")
    void whenFindAllLinksByCompany_andNotExistingCompany_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.findAllByCompanyId(userWithRoleUser, 0, 0, 2L));

        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider, never()).findAllByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a course then verify call find all courses by company id by page")
    void whenFindAllLinksByCourse_thenVerifyCallFindAllCoursesByCompanyIdByPage() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.findAllCoursesByCompanyId(adminUser, 0, 0, 1L);

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllCoursesByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a course as a non-admin user of the platform or the company, a forbidden action exception is returned.")
    void whenFindAllLinksByCourse_asNonAdminUserOfPlatformOrCompany_thenThrowForbiddenActionException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenThrow(ForbiddenActionException.class);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.findAllCoursesByCompanyId(adminUser, 0, 0, 1L));

        //THEN
        verify(checker).isAdminOrCompanyUser(any(User.class), anyLong());
        verify(checker, never()).companyExists(anyLong());
        verify(companyCourseProvider, never()).findAllCoursesByCompanyIdByPage(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When searching a company course id, then verify call find by company id and course id")
    void searchCompanyCourseId_thenVerifyCallFindByCompanyIdAndCourseId() throws ResourceNotFoundException {
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1, 2L, 3L, true, LocalDateTime.now(), null);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        long response = companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(1, 1);

        //THEN
        assertThat(response).isEqualTo(linkCompanyCourse.getId());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("given checker call companyCourseExists and throw ResourceNotFoundException when getCompanyCourseIdIfCourseIsActive then throw ResourceNotFoundException")
    void givenNotExistingCompanyCourse_whenGetCompanyCourseId_IfCourseIsActive_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.companyCourseExists(anyLong(), anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.getLinkCompanyCourseIdIfCourseIsActive(1, 1));

        verify(checker).companyCourseExists(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want to delete a link between a course and a company as the platform or company admin, then verify call delete")
    void whenDeleteLink_asPlatformOrCompanyAdmin_thenVerifyCallDelete() throws ForbiddenActionException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);
        Enrollment enrollment = new Enrollment();
        enrollment.setCompanyCourseId(linkCompanyCourse.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong());
        verify(companyCourseProvider).delete(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When want to suspend a link between a course and a company as the platform or company admin, then verify call save with correct parameters")
    void whenSuspendLink_asPlatformOrCompanyAdmin_thenVerifyCallSave() throws ForbiddenActionException {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        companyCourseService.unlink(adminUser, linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());

        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isFalse();
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When want to suspend a link between a course and a company as the platform or company admin and link does not exist, then verify not call save")
    void whenSuspendLink_asPlatformOrCompanyAdmin_andNotExistingLink_thenVerifyNotCallSave() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyCourseProvider.findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        //WHEN
        companyCourseService.unlink(adminUser, 1L, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider).findByCompanyIdAndCourseId(anyLong(), anyLong(), anyBoolean());
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When want to suspend a link between a course and a company as a non-admin user of the platform or the company, a forbidden action exception is returned.")
    void whenSuspendLink_asNonAdminPlatformOrCompany_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlink(userWithRoleUser, 2L, 2L, false));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).delete(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When want to delete all links of a company as the platform or company admin, then verify call delete all by company id")
    void whenDeleteAllLinksOfCompany_thenVerifyCallDeleteAllByCompanyId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.unlinkAllByCompanyId(adminUser, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("When suspend all links between courses and a company, then verify that the save method is called 2 times.")
    void whenUnlinkAllCoursesByCompanyId_thenVerify2CallSave() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        List<LinkCompanyCourse> list = new ArrayList<>();
        list.add(new LinkCompanyCourse(1L, 1L, 1L, true, null, null));
        list.add(new LinkCompanyCourse(1L, 1L, 2L, true, null, null));

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCompanyId(anyLong())).thenReturn(list);

        //WHEN
        companyCourseService.unlinkAllByCompanyId(adminUser, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyCourseProvider).findAllByCompanyId(anyLong());
        verify(companyCourseProvider, times(2)).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When want to delete all links of a company as a non-admin user of the platform or the company, a forbidden action exception is returned.")
    void whenDeleteAllLinksOfCompany_asNonAdminPlatformOrCompany_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCompanyId(userWithRoleUser, 2L, true));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("When want to delete all links of a company as the platform or company admin and the company does not exist,a resource not found exception is returned.")
    void whenDeleteAllLinksOfCompany_forNotExistingCompany_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCompanyId(adminUser, 2L, true));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyCourseProvider, never()).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("When want to delete all links of a course as an admin user, then verify call delete all by course id")
    void whenDeleteAllLinksOfCourse_asAdminUser_thenVerifyCallDeleteAllByCourseId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);

        //WHEN
        companyCourseService.unlinkAllByCourseId(userWithRoleUser, 1L, true);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).deleteAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("When suspend a link between a course and all companies, then verify that the save method is called 2 times.")
    void whenUnlinkACourseOfAllCompanies_thenVerify2CallSave() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        List<LinkCompanyCourse> list = new ArrayList<>();
        list.add(new LinkCompanyCourse(1L, 1L, 1L, true, null, null));
        list.add(new LinkCompanyCourse(1L, 1L, 2L, true, null, null));

        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.courseExists(anyLong())).thenReturn(true);
        when(companyCourseProvider.findAllByCourseId(anyLong())).thenReturn(list);

        //WHEN
        companyCourseService.unlinkAllByCourseId(adminUser, 1L, false);

        //THEN
        verify(checker).isAdminUser(any(User.class));
        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider).findAllByCourseId(anyLong());
        verify(companyCourseProvider, times(2)).save(any(LinkCompanyCourse.class));
    }

    @Test
    @DisplayName("When want to delete all links of a course as an non-admin user, a forbidden action exception is returned.")
    void whenDeleteAllLinksOfCourse_asNonAdminUser_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(userWithRoleUser, 2L, true));

        verify(checker).isAdminUser(any(User.class));
        verify(companyCourseProvider, never()).deleteAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("When want to delete all links of a course and course does not exist, a resource not found exception is returned.")
    void whenDeleteAllLinksOfCourse_forNotExistingCourse_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.courseExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyCourseService.unlinkAllByCourseId(adminUser, 2L, true));

        verify(checker).courseExists(anyLong());
        verify(companyCourseProvider, never()).deleteAllByCourseId(anyLong());
    }

    @Test
    @DisplayName("When suspend link of a course to a company, then verify that the link is saved with the correct parameters")
    void whenSuspendLink_thenVerifyCallSave() {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L, 1L, 1L, true, LocalDateTime.now().minusMonths(1), null);

        //WHEN
        companyCourseService.suspendLink(linkCompanyCourse);

        //THEN
        ArgumentCaptor<LinkCompanyCourse> captor = ArgumentCaptor.forClass(LinkCompanyCourse.class);
        verify(companyCourseProvider).save(captor.capture());
        LinkCompanyCourse linkCompanyCourseCaptured = captor.getValue();
        assertThat(linkCompanyCourseCaptured.isActive()).isFalse();
        assertThat(linkCompanyCourseCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When suspend link of a course to the company and the link is inactive, then verify do not call save")
    void whenSuspendLinkInactive_thenVerifyNotCallSave() {
        //GIVEN
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1L,1L, 1L, false, LocalDateTime.now(), LocalDateTime.now());

        //WHEN
        companyCourseService.suspendLink(linkCompanyCourse);

        //THEN
        verify(companyCourseProvider, never()).save(any(LinkCompanyCourse.class));
    }

}