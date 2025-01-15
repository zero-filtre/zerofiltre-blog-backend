package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

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
        companyUserService = new CompanyUserService(companyUserProvider, checker);
    }

    @Test
    @DisplayName("When link a user to a company, then verify call save")
    void whenLinkUserToCompany_thenVerifyCallSave() throws ForbiddenActionException, ResourceNotFoundException {
        //WHEN
        companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(companyUserProvider).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When link a user to a company and don't have permission, a forbidden action exception is returned.")
    void givenNotHasPermission_whenLink_thenThrowResourceNotFoundException() throws ForbiddenActionException {
        //GIVEN
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When link a user to a company and the company does not exist, a resource not found exception is returned.")
    void whenLink__notExistingCompany_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When link a user to a company as an admin user and the course does not exist, a resource not found exception is returned.")
    void whenLink_notExistingUser_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.userExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(companyUserProvider, never()).save(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When want to find a link between a user and a company as the platform or company admin, verify that the method to find by the company id and user id is called")
    void whenFindLink_thenVerifyCallFindByCompanyIdAndUserId() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.userExists(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.find(adminUser, 1L, 1L);

        //THEN
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want to find a link between a user and a company and the company does not exist, a resource not found exception is returned.")
    void whenFindLink_NotExistingCompany_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want to find a link between a user and a company and the user does not exist, a resource not found exception is returned.")
    void whenFindLink_NotExistingUser_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.userExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want to find a link between a user and a company as a non-admin user of the platform or the company, a forbidden action exception is returned.")
    void whenFindLink_asNonAdminUserPlatformOrCompany_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a company then verify call find all by company id")
    void whenFindAllLinksByCompany_thenVerifyCallFindAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);

        //WHEN
        companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L);

        //THEN
        verify(companyUserProvider).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a company and the company does not exist, a resource not found exception is returned.")
    void whenFindAllLinksByCompany_andNotExistingCompany_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L));

        //THEN
        verify(companyUserProvider, never()).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When want find all links of a company as a non-admin user of the platform or the company, a forbidden action exception is returned.")
    void whenFindAllLinksByCompany_asNonAdminUserPlatformOrCompany_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L));

        //THEN
        verify(companyUserProvider, never()).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("When want to delete a link between a user and a company as the platform or company admin, verify that the method to delete is called")
    void whenDeleteLink_asPlatformOrCompanyAdmin_thenVerifyCallDelete() throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser()));
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);

        //WHEN
        companyUserService.unlink(adminUser, 1L, 1L);

        //THEN
        verify(companyUserProvider).delete(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When want to delete a link between a user and a company and company user does not exist, verify that the method to delete is not called")
    void whenDeleteLink_andNotExistingCompanyUser_thenVerifyNotCallDelete() throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyUserService.unlink(adminUser, 1L, 1L);

        //THEN
        verify(companyUserProvider, never()).delete(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("When want to delete a link between a user and a company without permission, a forbidden action exception is returned.")
    void givenNotHasPermission_whenUnlink_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser()));
        when(checker.hasPermission(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlink(userWithUserRole, 2L, 2L));

        verify(companyUserProvider, never()).delete(any(LinkCompanyUser.class));
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
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.ADMIN)));

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
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.EDITOR)));

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
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.ADMIN)));

        //WHEN
        boolean response = companyUserService.isCompanyAdmin(adminUser, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given company user with editor role when isCompanyAdmin then return false")
    void givenCompanyUserWithEditorRole_whenIsCompanyAdmin_thenReturnTrue() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.EDITOR)));

        //WHEN
        boolean response = companyUserService.isCompanyAdmin(adminUser, 1L);

        //THEN
        assertThat(response).isFalse();
    }

}