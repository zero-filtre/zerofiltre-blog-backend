package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyUserServiceTest {

    private static User adminUser;
    private static User userWithUserRole;

    private CompanyUserService companyUserService;

    @Mock
    CompanyUserProvider companyUserProvider;

    @Mock
    EnrollmentProvider enrollmentProvider;

    @Mock
    DataChecker checker;

    @BeforeAll
    static void setup() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.getRoles().add("ROLE_ADMIN");

        userWithUserRole = new User();
        userWithUserRole.setId(1L);
        userWithUserRole.getRoles().add("ROLE_USER");
    }

    @BeforeEach
    void init() {
        companyUserService = new CompanyUserService(companyUserProvider, enrollmentProvider, checker);
    }

    @Test
    @DisplayName("When a user with permission links a user to a company, then the link is created")
    void shouldCreatesLink_whenLinkUserToCompany_asUserWithPermission() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));
        verify(checker).companyExists(anyLong());
        verify(checker).userExists(anyLong());

        ArgumentCaptor<LinkCompanyUser> captor = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(companyUserProvider).save(captor.capture());
        LinkCompanyUser linkCompanyUserCaptured = captor.getValue();
        assertThat(linkCompanyUserCaptured.isActive()).isTrue();
        assertThat(linkCompanyUserCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyUserCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When a user with permission links a user to a company and the link already exists, then there is nothing")
    void shouldDoNothing_whenLinkUserToCompany_IfLinkExists_asUserWithPermission() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now().minusMonths(1), null);

        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyUser));

        //WHEN
        companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));
        verify(checker).companyExists(anyLong());
        verify(checker).userExists(anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a link between a user and a company is suspended and a user with permission links them again, then the link is activated")
    void shouldActivatesLink_whenSuspendLinkBetweenUserAndCompany_LinkAgain_asUserWithPermission() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.EDITOR, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusDays(10));

        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyUser));

        //WHEN
        companyUserService.link(adminUser, linkCompanyUser.getCompanyId(), linkCompanyUser.getUserId(), LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));
        verify(checker).companyExists(anyLong());
        verify(checker).userExists(anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());

        ArgumentCaptor<LinkCompanyUser> captor = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(companyUserProvider).save(captor.capture());
        LinkCompanyUser linkCompanyUserCaptured = captor.getValue();
        assertThat(linkCompanyUserCaptured.isActive()).isTrue();
        assertThat(linkCompanyUserCaptured.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(linkCompanyUserCaptured.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("When a user without permission links a user to a company, then it is forbidden")
    void shouldForbidden_whenLinkUserToCompany_asUserWithoutPermission()
    throws ForbiddenActionException {
        //GIVEN
        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.link(userWithUserRole, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a user with permission links a user to a non-existent company, then the user and the company are not linked")
    void shouldUserAndCompanyNotLinked_whenLinkUserToNotExistingCompany_asUserWithPermission() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a user with permission links a non-existent user to a company, then the user and the company are not linked")
    void shouldUserAndCompanyNotLinked_whenLinkNotExistingUserToCompany_asUserWithPermission() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a platform or company admin searches for a link between a user and a company, then the link is found")
    void shouldFindsLink_whenSearchForLinkBetweenUserAndCompany_asAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.find(adminUser, 1L, 1L);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(checker).userExists(anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a non-admin user of the platform or company searches for a link between a user and a company, it is forbidden")
    void shouldForbidden_whenSearchForLinkBetweenUserAndCompany_asNonPlatformOrCompanyAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.find(userWithUserRole, 1L, 1L));

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin searches for a link between a user and a non-existent company, then he finds nothing")
    void shouldFindNothing_whenSearchForLinkBetweenUserAndNotExistingCompany_asPlatformOrCompanyAdmin() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin searches for a link between a non-existent user and a company, then he finds nothing")
    void shouldFindNothing_whenSearchForLinkBetweenNotExistingUserAndCompany_asPlatformOrCompanyAdmin() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(checker).userExists(anyLong());
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin searches for all the links between users and a company, then he finds a part of the list of links")
    void shouldFindPartOfLinkList_whenSearchingForAllLinksBetweenUsersAndCompany_asPlatformOrCompanyAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When a non-admin user of the platform or the company searches all links between users and a company, then it is forbidden")
    void shouldForbidden_whenSearchingAllLinksBetweenUsersAndCompany_asNonAdminPlatformOrCompany() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.findAllByCompanyId(userWithUserRole, 0, 10, 1L));

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider, never()).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When a non-admin user of the platform or the company searches all links between users and a company, then he finds nothing")
    void shouldFindNothing_whenSearchingAllLinksBetweenUsersAndNonExistingCompany_asPlatformOrCompanyAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L));

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider, never()).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When I search for the ID of an active link between a user and a company, the link is found")
    void shouldFindIdLinkBetweenUserAndCompany() throws ResourceNotFoundException {
        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1, 2L, 3L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(linkCompanyUser));

        //WHEN
        long response = companyUserService.getLinkCompanyUserIdIfUserIsActive(1, 1);

        //THEN
        assertThat(response).isEqualTo(linkCompanyUser.getId());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("When I search for the ID of an non-existent link between a user and a company, then I find nothing")
    void shouldFindNothing_whenSearchingIdNonExistingLinkBetweenUserAndCompany() throws ResourceNotFoundException {
        //GIVEN
        when(checker.companyUserExists(anyLong(), anyLong())).thenThrow(ResourceNotFoundException.class);

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.getLinkCompanyUserIdIfUserIsActive(1, 1));

        verify(checker).companyUserExists(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin deletes the link between a user and a company, the link is deleted and the enrollments related to this link are suspended")
    void shouldDeleteLinkAndSuspendEnrollments_whenLinkBetweenUserAndCompanyIsDeleted_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        CompanyUserService spy = spy(companyUserService);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1, 2L, 3L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null);

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyUser));
        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        doNothing().when(spy).suspendEnrollments(anyLong());

        //WHEN
        spy.unlink(adminUser, 1L, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));
        verify(companyUserProvider).delete(any(LinkCompanyUser.class));

        ArgumentCaptor<Long> captorLink = ArgumentCaptor.forClass(Long.class);
        verify(spy).suspendEnrollments(captorLink.capture());
        long linkIdCaptured = captorLink.getValue();
        assertThat(linkIdCaptured).isEqualTo(linkCompanyUser.getId());

        verify(spy).suspendEnrollments(linkCompanyUser.getId());
    }

    @Test
    @DisplayName("When a platform or company admin deletes a non-existent link between a user and a company, then there is nothing")
    void shouldNothing_whenDeleteNotExistingLinkBetweenUserAndCompany_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        CompanyUserService spy = spy(companyUserService);

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        spy.unlink(adminUser, 1L, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(companyUserProvider, never()).delete(any(LinkCompanyUser.class));
        verify(spy, never()).suspendEnrollments(anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin suspends the link between a user and a company, the link is suspended and the enrollments related to this link are suspended")
    void shouldSuspendLinkAndSuspendEnrollments_whenLinkBetweenUserAndCompanyIsSuspended_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        CompanyUserService spy = spy(companyUserService);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1, 2L, 3L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyUserId(linkCompanyUser.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyUserId(linkCompanyUser.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyUser));
        when(checker.hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(enrollmentProvider.findAllByCompanyUserId(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));

        //WHEN
        spy.unlink(adminUser, 1L, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(checker).hasPermission(any(User.class), anyLong(), any(LinkCompanyUser.Role.class));

        ArgumentCaptor<LinkCompanyUser> captorLink = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(spy).suspendLink(captorLink.capture());
        LinkCompanyUser linkCaptured = captorLink.getValue();
        assertThat(linkCaptured).isNotNull();
        assertThat(linkCaptured.getId()).isEqualTo(linkCompanyUser.getId());
        assertThat(linkCaptured.getCompanyId()).isEqualTo(linkCompanyUser.getCompanyId());
        assertThat(linkCaptured.getUserId()).isEqualTo(linkCompanyUser.getUserId());
        assertThat(linkCaptured.isActive()).isEqualTo(linkCompanyUser.isActive());
        assertThat(linkCaptured.getLinkedAt()).isEqualTo(linkCompanyUser.getLinkedAt());
        assertThat(linkCaptured.getSuspendedAt()).isEqualTo(linkCompanyUser.getSuspendedAt());

        verify(spy).suspendEnrollments(linkCompanyUser.getId());
    }

    @Test
    @DisplayName("When a platform or company admin suspends a non-existent link between a user and a company, then there is nothing")
    void shouldNothing_whenUnlinkNotExistingLinkBetweenUserAndCompany_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        CompanyUserService spy = spy(companyUserService);

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        spy.unlink(adminUser, 1L, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(spy, never()).suspendLink(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a platform admin deletes all links between users and a company, then the links are deleted and the enrollments related to these links are suspended")
    void shouldDeleteAllLinksAndSuspendEnrollments_whenAllLinksBetweenUsersAndCompanyAreDeleted_asPlatformAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyUser linkCompanyUser1 = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyUser linkCompanyUser2 = new LinkCompanyUser(2L, 1L, 2L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyUserId(linkCompanyUser2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyUserId(linkCompanyUser2.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findAllByCompanyId(anyLong())).thenReturn(List.of(linkCompanyUser1, linkCompanyUser2));
        when(enrollmentProvider.findAllByCompanyUserId(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyUserService.unlinkAllByCompanyId(adminUser, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider).findAllByCompanyId(anyLong());

        ArgumentCaptor<LinkCompanyUser> captorLink = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(companyUserProvider, times(2)).delete(captorLink.capture());
        List<LinkCompanyUser> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyUser1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getUserId()).isEqualTo(linkCompanyUser1.getUserId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyUser1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyUser1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyUser1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyUser2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getUserId()).isEqualTo(linkCompanyUser2.getUserId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyUser2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyUser2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyUser2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When a company admin deletes all the links between users and a company, then all links, except company admin, are deleted and the enrollments related to these links are suspended")
    void shouldDeleteAllLinksExceptCompanyAdminAndSuspendEnrollments_whenAllLinksBetweenUsersAndCompanyAreDeleted_asCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyUser linkCompanyUser1 = new LinkCompanyUser(1L, 1L, 4L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyUser linkCompanyUser2 = new LinkCompanyUser(2L, 1L, 5L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyUserId(linkCompanyUser2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyUserId(linkCompanyUser2.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findAllByCompanyIdExceptAdminRole(anyLong())).thenReturn(List.of(linkCompanyUser1, linkCompanyUser2));
        when(enrollmentProvider.findAllByCompanyUserId(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyUserService.unlinkAllByCompanyId(userWithUserRole, 1L, true);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider).findAllByCompanyIdExceptAdminRole(anyLong());

        ArgumentCaptor<LinkCompanyUser> captorLink = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(companyUserProvider, times(2)).delete(captorLink.capture());
        List<LinkCompanyUser> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyUser1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getUserId()).isEqualTo(linkCompanyUser1.getUserId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyUser1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyUser1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyUser1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyUser2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getUserId()).isEqualTo(linkCompanyUser2.getUserId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyUser2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyUser2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyUser2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When a platform admin suspends all links between users and a company, then the links are suspended and the enrollments related to these links are suspended")
    void shouldSuspendAllLinksAndSuspendEnrollments_whenAllLinksBetweenUsersAndCompanyAreSuspended_asPlatformAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyUser linkCompanyUser1 = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyUser linkCompanyUser2 = new LinkCompanyUser(2L, 1L, 2L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyUserId(linkCompanyUser2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyUserId(linkCompanyUser2.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findAllByCompanyId(anyLong())).thenReturn(List.of(linkCompanyUser1, linkCompanyUser2));
        when(enrollmentProvider.findAllByCompanyUserId(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyUserService.unlinkAllByCompanyId(adminUser, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider).findAllByCompanyId(anyLong());

        ArgumentCaptor<LinkCompanyUser> captorLink = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(companyUserProvider, times(2)).save(captorLink.capture());
        List<LinkCompanyUser> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyUser1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getUserId()).isEqualTo(linkCompanyUser1.getUserId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyUser1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyUser1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyUser1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyUser2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getUserId()).isEqualTo(linkCompanyUser2.getUserId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyUser2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyUser2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyUser2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When a company admin suspends all the links between users and a company, then all links, except company admin, are suspended and the enrollments related to these links are suspended")
    void shouldSuspendAllLinksExceptCompanyAdminAndSuspendEnrollments_whenAllLinksBetweenUsersAndCompanyAreSuspended_asCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        LinkCompanyUser linkCompanyUser1 = new LinkCompanyUser(1L, 1L, 4L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now().minusMonths(1), null);

        LinkCompanyUser linkCompanyUser2 = new LinkCompanyUser(2L, 1L, 5L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyUserId(linkCompanyUser1.getId());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setCompanyUserId(linkCompanyUser2.getId());

        Enrollment enrollment4 = new Enrollment();
        enrollment4.setCompanyUserId(linkCompanyUser2.getId());

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findAllByCompanyIdExceptAdminRole(anyLong())).thenReturn(List.of(linkCompanyUser1, linkCompanyUser2));
        when(enrollmentProvider.findAllByCompanyUserId(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2)).thenReturn(List.of(enrollment3, enrollment4));

        //WHEN
        companyUserService.unlinkAllByCompanyId(userWithUserRole, 1L, false);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider).findAllByCompanyIdExceptAdminRole(anyLong());

        ArgumentCaptor<LinkCompanyUser> captorLink = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(companyUserProvider, times(2)).save(captorLink.capture());
        List<LinkCompanyUser> listLinkCaptured = captorLink.getAllValues();
        assertThat(listLinkCaptured).isNotNull();
        assertThat(listLinkCaptured.get(0).getId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listLinkCaptured.get(0).getCompanyId()).isEqualTo(linkCompanyUser1.getCompanyId());
        assertThat(listLinkCaptured.get(0).getUserId()).isEqualTo(linkCompanyUser1.getUserId());
        assertThat(listLinkCaptured.get(0).isActive()).isEqualTo(linkCompanyUser1.isActive());
        assertThat(listLinkCaptured.get(0).getLinkedAt()).isEqualTo(linkCompanyUser1.getLinkedAt());
        assertThat(listLinkCaptured.get(0).getSuspendedAt()).isEqualTo(linkCompanyUser1.getSuspendedAt());

        assertThat(listLinkCaptured.get(1).getId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listLinkCaptured.get(1).getCompanyId()).isEqualTo(linkCompanyUser2.getCompanyId());
        assertThat(listLinkCaptured.get(1).getUserId()).isEqualTo(linkCompanyUser2.getUserId());
        assertThat(listLinkCaptured.get(1).isActive()).isEqualTo(linkCompanyUser2.isActive());
        assertThat(listLinkCaptured.get(1).getLinkedAt()).isEqualTo(linkCompanyUser2.getLinkedAt());
        assertThat(listLinkCaptured.get(1).getSuspendedAt()).isEqualTo(linkCompanyUser2.getSuspendedAt());

        ArgumentCaptor<Enrollment> captorEnrollment = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(4)).save(captorEnrollment.capture());
        List<Enrollment> listEnrollmentCaptured = captorEnrollment.getAllValues();
        assertThat(listEnrollmentCaptured.size()).isNotZero();

        assertThat(listEnrollmentCaptured.get(0).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(0).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(1).getCompanyUserId()).isEqualTo(linkCompanyUser1.getId());
        assertThat(listEnrollmentCaptured.get(1).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(2).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(2).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(2).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(listEnrollmentCaptured.get(3).getCompanyUserId()).isEqualTo(linkCompanyUser2.getId());
        assertThat(listEnrollmentCaptured.get(3).isActive()).isFalse();
        assertThat(listEnrollmentCaptured.get(3).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("When a non-admin user of the platform or company deletes all links between users and a company, then it is forbidden")
    void shouldForbidden_whenDeleteAllLinksBetweenCoursesAndCompany_asPlatformOrCompanyNonAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByCompanyId(adminUser, 1L, true));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider, never()).findAllByCompanyId(anyLong());
        verify(companyUserProvider, never()).findAllByCompanyIdExceptAdminRole(anyLong());
    }

    @Test
    @DisplayName("When a non-admin user of the platform or company suspends all links between users and a company, then it is forbidden")
    void shouldForbidden_whenSuspendAllLinksBetweenCoursesAndCompany_asNotPlatformOrCompanyAdmin() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByCompanyId(adminUser, 1L, false));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider, never()).findAllByCompanyId(anyLong());
        verify(companyUserProvider, never()).findAllByCompanyIdExceptAdminRole(anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin deletes all links between users and a non-existent company, then there is nothing")
    void shouldNothing_whenDeleteAllLinksOfNotExistingCompany_asPlatformOrCompanyAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByCompanyId(adminUser, 1L, true));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider, never()).findAllByCompanyId(anyLong());
        verify(companyUserProvider, never()).findAllByCompanyIdExceptAdminRole(anyLong());
    }

    @Test
    @DisplayName("When a platform or company admin suspends all links between users and a non-existent company, then there is nothing")
    void shouldNothing_whenSuspendAllLinksOfNotExistingCompany_asPlatformOrCompanyAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByCompanyId(adminUser, 1L, false));

        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider, never()).findAllByCompanyId(anyLong());
        verify(companyUserProvider, never()).findAllByCompanyIdExceptAdminRole(anyLong());
    }

    @Test
    @DisplayName("given admin user and existing user when unlinkAllByUserId then verify call companyUserProvider unlinkAllByUserId")
    void givenAdminUser_whenUnlinkAllByUserId_thenVerifyCallCompanyUserProviderCompanyProviderUnlinkAllByUserId() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);

        //WHEN
        companyUserService.unlinkAllByUserId(adminUser, 1L);

        //THEN
        verify(companyUserProvider).deleteAllByUserId(anyLong());
    }

    @Test
    @DisplayName("given user with role user when unlinkAllByUserId then throw ForbiddenActionException")
    void givenUserWithUserRole_whenUnlinkAllByUserId_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByUserId(userWithUserRole, 2L));

        //THEN
        verify(companyUserProvider, never()).deleteAllByUserId(anyLong());
    }

    @Test
    @DisplayName("When suspend all links between a user and all companies and user does not exist, a forbidden action exception is returned.")
    void whenUnlinkAllUsersOfCompanies_andNotExistingUser_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByUserId(adminUser, 2L));

        verify(companyUserProvider, never()).deleteAllByUserId(anyLong());
    }

    @Test
    @DisplayName("When suspend link of a user to a company, then verify that the link is saved with the correct parameters and save enrollment")
    void whenSuspendLink_thenVerifyCallSave() throws ZerofiltreException {
        //GIVEN
        CompanyUserService spy = spy(companyUserService);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now().minusMonths(1), null);

        Enrollment enrollment = new Enrollment();
        enrollment.setCompanyUserId(linkCompanyUser.getId());

        //WHEN
        spy.suspendLink(linkCompanyUser);

        //THEN
        ArgumentCaptor<LinkCompanyUser> captor = ArgumentCaptor.forClass(LinkCompanyUser.class);
        verify(companyUserProvider).save(captor.capture());
        LinkCompanyUser linkCompanyUserCaptured = captor.getValue();
        assertThat(linkCompanyUserCaptured.isActive()).isFalse();
        assertThat(linkCompanyUserCaptured.getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(spy).suspendEnrollments(anyLong());
    }

    @Test
    @DisplayName("When suspend link of a user to the company and the link is inactive, then verify do not call save")
    void whenSuspendLinkInactive_thenVerifyNotCallSave() throws ZerofiltreException {
        //GIVEN
        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1L,1L, 1L, LinkCompanyUser.Role.VIEWER, false, LocalDateTime.now(), LocalDateTime.now());

        //WHEN
        companyUserService.suspendLink(linkCompanyUser);

        //THEN
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When suspend all enrollments of a user to a company, then verify that the enrollments are saved with the correct parameters")
    void whenSuspendEnrollment_thenVerifyCallSaveEnrollments() throws ZerofiltreException {
        //GIVEN
        long companyUserId = 1;

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyUserId(companyUserId);
        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyUserId(companyUserId);

        List<Enrollment> enrollmentList = List.of(enrollment1, enrollment2);

        when(enrollmentProvider.findAllByCompanyUserId(companyUserId, true)).thenReturn(enrollmentList);

        //WHEN
        companyUserService.suspendEnrollments(companyUserId);

        //THEN
        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentProvider, times(2)).save(captor.capture());
        List<Enrollment> enrollmentsCaptured = captor.getAllValues();
        assertThat(enrollmentsCaptured.size()).isEqualTo(2);
        assertThat(enrollmentsCaptured.get(0).isActive()).isFalse();
        assertThat(enrollmentsCaptured.get(0).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(enrollmentsCaptured.get(1).isActive()).isFalse();
        assertThat(enrollmentsCaptured.get(1).getSuspendedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

}