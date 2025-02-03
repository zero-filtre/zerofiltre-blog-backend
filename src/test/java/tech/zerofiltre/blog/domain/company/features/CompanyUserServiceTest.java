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
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(checker).hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class));
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

        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty()).thenReturn(Optional.of(linkCompanyUser));

        //WHEN
        companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(checker).hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class));
        verify(checker).companyExists(anyLong());
        verify(checker).userExists(anyLong());
        verify(companyUserProvider, times(2)).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a link between a user and a company is suspended and a user with permission links them again, then the link is activated")
    void shouldActivatesLink_whenSuspendLinkBetweenUserAndCompany_LinkAgain_asUserWithPermission() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.EDITOR, false, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusDays(10));

        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty()).thenReturn(Optional.of(linkCompanyUser));

        //WHEN
        companyUserService.link(adminUser, linkCompanyUser.getCompanyId(), linkCompanyUser.getUserId(), LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(checker).hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class));
        verify(checker).companyExists(anyLong());
        verify(checker).userExists(anyLong());
        verify(companyUserProvider, times(2)).findByCompanyIdAndUserId(anyLong(), anyLong());

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
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.link(userWithUserRole, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(checker).hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class));
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a user with permission links a user to a non-existent company, then the user and the company are not linked")
    void shouldUserAndCompanyNotLinked_whenLinkUserToNotExistingCompany_asUserWithPermission() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(checker).hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class));
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a user with permission links a non-existent user to a company, then the user and the company are not linked")
    void shouldUserAndCompanyNotLinked_whenLinkNotExistingUserToCompany_asUserWithPermission() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(checker).hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class));
        verify(checker).companyExists(anyLong());
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When a platform or company admin searches for a link between a course and a company, then the link is found")
    void shouldFindsLink_whenSearchForLinkBetweenCourseAndCompany_asAdminOrCompanyUser() throws ForbiddenActionException, ResourceNotFoundException {
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
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);
        when(enrollmentProvider.findAllByCompanyUserId(anyLong(), anyBoolean())).thenReturn(List.of(enrollment1, enrollment2));

        //WHEN
        spy.unlink(adminUser, 1L, 1L);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider, times(2)).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(checker).hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class));

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

        verify(spy).suspendEnrollment(linkCompanyUser.getId());
    }

    @Test
    @DisplayName("When a platform or company admin suspends a non-existent link between a user and a company, then there is nothing")
    void shouldNothing_whenUnlinkNotExistingLinkBetweenUserAndCompany_asPlatformOrCompanyAdmin() throws ZerofiltreException {
        //GIVEN
        CompanyUserService spy = spy(companyUserService);

        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        spy.unlink(adminUser, 1L, 1L);

        //THEN
        verify(checker).isAdminOrCompanyAdmin(any(User.class), anyLong());
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
        verify(spy, never()).suspendLink(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("given a admin user when unlinkAllByCompanyId then verify call companyUserProvider unlinkAllByCompanyId")
    void givenAdminUser_whenUnlinkAllByCompanyId_thenVerifyCallCompanyUserProviderUnlinkAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.unlinkAllByCompanyId(adminUser, 1L);

        //THEN
        verify(companyUserProvider).deleteAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("given a user with company admin role when unlinkAllByCompanyId then verify call companyUserProvider unlinkAllByCompanyIdExceptAdminRole")
    void givenUserWithCompanyAdminRole_whenUnlinkAllByCompanyIdExceptAdminRole_thenVerifyCallCompanyUserProviderUnlinkAllByCompanyIdExceptAdminRole() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null)));

        //WHEN
        companyUserService.unlinkAllByCompanyId(userWithUserRole, 1L);

        //THEN
        verify(companyUserProvider).deleteAllByCompanyIdExceptAdminRole(anyLong());
    }

    @Test
    @DisplayName("if a connected user is not admin or company admin when unlink all users for a company then throw an exception")
    void givenNotAdminOrCompanyAdminUser_whenUnlinkAllByCompanyId_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByCompanyId(adminUser, 1L));
    }

    @Test
    @DisplayName("given non existent companyId when unlinkAllByCompanyId then throw ResourceNotFoundException")
    void givenNonExistentCompanyId_whenUnlinkAllByCompanyId_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByCompanyId(adminUser, 1L));
    }

    @Test
    @DisplayName("given a user with company editor role when unlinkAllByCompanyId then nothing")
    void givenUserWithCompanyEditorRole_whenUnlinkAllByCompanyId_thenNothing() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null)));

        //WHEN
        companyUserService.unlinkAllByCompanyId(userWithUserRole, 1L);

        //THEN
        verify(companyUserProvider, never()).deleteAllByCompanyId(anyLong());
        verify(companyUserProvider, never()).deleteAllByCompanyIdExceptAdminRole(anyLong());
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
    @DisplayName("given company user with admin role when isCompanyAdmin then return true")
    void givenCompanyUserWithAdminRole_whenIsCompanyAdmin_thenReturnTrue() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null)));

        //WHEN
        boolean response = companyUserService.isCompanyAdmin(adminUser, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given company user with editor role when isCompanyAdmin then return false")
    void givenCompanyUserWithEditorRole_whenIsCompanyAdmin_thenReturnTrue() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, 1L, LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null)));

        //WHEN
        boolean response = companyUserService.isCompanyAdmin(adminUser, 1L);

        //THEN
        assertThat(response).isFalse();
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

        verify(spy).suspendEnrollment(anyLong());
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
    void whenSuspendEnrollment_thenVerifyCallSaveEnrollment() throws ZerofiltreException {
        //GIVEN
        long companyUserId = 1;

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCompanyUserId(companyUserId);
        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCompanyUserId(companyUserId);

        List<Enrollment> enrollmentList = List.of(enrollment1, enrollment2);

        when(enrollmentProvider.findAllByCompanyUserId(companyUserId, true)).thenReturn(enrollmentList);

        //WHEN
        companyUserService.suspendEnrollment(companyUserId);

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