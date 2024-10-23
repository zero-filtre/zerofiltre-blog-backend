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
import tech.zerofiltre.blog.domain.user.features.IsAdminUser;
import tech.zerofiltre.blog.domain.user.features.IsUserExists;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkCompanyUserServiceTest {

    private static User adminUser;
    private static User userWithUserRole;

    private CompanyUserService companyUserService;

    @Mock
    CompanyUserProvider companyUserProvider;

    @Mock
    IsAdminOrCompanyAdmin isAdminOrCompanyAdmin;

    @Mock
    HasPermission hasPermission;

    @Mock
    IsAdminUser isAdminUser;

    @Mock
    IsCompanyExists isCompanyExists;

    @Mock
    IsUserExists isUserExists;

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
        companyUserService = new CompanyUserService(companyUserProvider, isAdminUser, isCompanyExists, isUserExists, isAdminOrCompanyAdmin, hasPermission);
    }

    @Test
    @DisplayName("when link with role then verify call companyUserProvider link")
    void whenLink_thenVerifyCallCompanyUserProviderLink() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isUserExists.execute(anyLong())).thenReturn(true);
        when(hasPermission.execute(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);

        //WHEN
        companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN);

        //THEN
        verify(companyUserProvider).link(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("given non existent company when link then throw ResourceNotFoundException")
    void givenNonExistentCompany_whenLink_thenThrowResourceNotFoundException() throws ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(companyUserProvider, never()).link(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("given non existent user when link then throw ResourceNotFoundException")
    void givenNonExistentUser_whenLink_thenThrowResourceNotFoundException() throws ResourceNotFoundException {
        //GIVEN
        when(isUserExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(companyUserProvider, never()).link(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("given not has permission when link then throw ResourceNotFoundException")
    void givenNotHasPermission_whenLink_thenThrowResourceNotFoundException() throws ForbiddenActionException {
        //GIVEN
        when(hasPermission.execute(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.link(adminUser, 1L, 1L, LinkCompanyUser.Role.ADMIN));

        //THEN
        verify(companyUserProvider, never()).link(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("when find then verify call companyUserProvider find")
    void whenFind_thenVerifyCallCompanyUserProviderFind() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isUserExists.execute(anyLong())).thenReturn(true);
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);

        //WHEN
        companyUserService.find(adminUser, 1L, 1L);

        //THEN
        verify(companyUserProvider).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("given non existent company when find then throw ResourceNotFoundException")
    void givenNonExistentCompany_whenFind_thenThrowResourceNotFoundException() throws ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("given non existent user when find then throw ResourceNotFoundException")
    void givenNonExistentUser_whenFind_thenThrowResourceNotFoundException() throws ResourceNotFoundException {
        //GIVEN
        when(isUserExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("given user not admin and not company admin when find then throw ResourceNotFoundException")
    void givenNotHasPermission_whenFind_thenThrowResourceNotFoundException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.find(adminUser, 1L, 1L));

        //THEN
        verify(companyUserProvider, never()).findByCompanyIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("when findAllByCompanyId then verify call companyUserProvider findAllByCompanyId")
    void whenFindAllByCompanyId_thenVerifyCallCompanyUserProviderFindAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenReturn(true);

        //WHEN
        companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L);

        //THEN
        verify(companyUserProvider).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("given non existent company when findAllByCompanyId then throw ResourceNotFoundException")
    void givenNonExistentCompany_whenFindAllByCompanyId_thenThrowResourceNotFoundException() throws ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L));

        //THEN
        verify(companyUserProvider, never()).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("given user not admin and not company admin when findAllByCompanyId then throw ResourceNotFoundException")
    void givenNotHasPermission_whenFindAllByCompanyId_thenThrowResourceNotFoundException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminOrCompanyAdmin.execute(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.findAllByCompanyId(adminUser, 0, 10, 1L));

        //THEN
        verify(companyUserProvider, never()).findAllByCompanyId(anyInt(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("when unlink then verify call companyUserProvider unlink")
    void whenUnlink_thenVerifyCallCompanyUserProviderUnlink() throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser()));
        when(hasPermission.execute(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenReturn(true);

        //WHEN
        companyUserService.unlink(adminUser, 1L, 1L);

        //THEN
        verify(companyUserProvider).unlink(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("given empty company user when unlink then verify not call companyUserProvider unlink")
    void givenEmptyCompanyUser_whenUnlink_thenVerifyNotCallCompanyUserProviderUnlink() throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //WHEN
        companyUserService.unlink(adminUser, 1L, 1L);

        //THEN
        verify(companyUserProvider, never()).unlink(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("given user not admin and not company admin when unLink then throw ForbiddenActionException")
    void givenNotHasPermission_whenUnlink_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser()));
        when(hasPermission.execute(any(User.class), anyBoolean(), any(LinkCompanyUser.Role.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlink(userWithUserRole, 2L, 2L));

        verify(companyUserProvider, never()).unlink(any(LinkCompanyUser.class));
    }

    @Test
    @DisplayName("given a admin user when unlinkAllByCompanyId then verify call companyUserProvider unlinkAllByCompanyId")
    void givenAdminUser_whenUnlinkAllByCompanyId_thenVerifyCallCompanyUserProviderUnlinkAllByCompanyId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.unlinkAllByCompanyId(adminUser, 1L);

        //THEN
        verify(companyUserProvider).unlinkAllByCompanyId(anyLong());
    }

    @Test
    @DisplayName("given a user with company admin role when unlinkAllByCompanyIdExceptAdminRole then verify call companyUserProvider unlinkAllByCompanyIdExceptAdminRole")
    void givenUserWithCompanyAdminRole_whenUnlinkAllByCompanyIdExceptAdminRole_thenVerifyCallCompanyUserProviderUnlinkAllByCompanyIdExceptAdminRole() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.ADMIN)));

        //WHEN
        companyUserService.unlinkAllByCompanyId(userWithUserRole, 1L);

        //THEN
        verify(companyUserProvider).unlinkAllByCompanyIdExceptAdminRole(anyLong());
    }

    @Test
    @DisplayName("given non existent companyId when unlinkAllByCompanyId then throw ResourceNotFoundException")
    void givenNonExistentCompanyId_whenUnlinkAllByCompanyId_thenThrowException() throws ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByCompanyId(adminUser, 1L));
    }

    @Test
    @DisplayName("given a user with company editor role when unlinkAllByCompanyId then nothing")
    void givenUserWithCompanyEditorRole_whenUnlinkAllByCompanyId_thenNothing() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isCompanyExists.execute(anyLong())).thenReturn(true);
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.EDITOR)));

        //WHEN
        companyUserService.unlinkAllByCompanyId(userWithUserRole, 1L);

        //THEN
        verify(companyUserProvider, never()).unlinkAllByCompanyId(anyLong());
        verify(companyUserProvider, never()).unlinkAllByCompanyIdExceptAdminRole(anyLong());
    }

    @Test
    @DisplayName("given admin user and existing user when unlinkAllByUserId then verify call companyUserProvider unlinkAllByUserId")
    void givenAdminUser_whenUnlinkAllByUserId_thenVerifyCallCompanyUserProviderCompanyProviderUnlinkAllByUserId() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isUserExists.execute(anyLong())).thenReturn(true);

        //WHEN
        companyUserService.unlinkAllByUserId(adminUser, 1L);

        //THEN
        verify(companyUserProvider).unlinkAllByUserId(anyLong());
    }

    @Test
    @DisplayName("given user with role user when unlinkAllByUserId then throw ForbiddenActionException")
    void givenUserWithUserRole_whenUnlinkAllByUserId_thenThrowException() throws ForbiddenActionException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByUserId(userWithUserRole, 2L));

        //THEN
        verify(companyUserProvider, never()).unlinkAllByUserId(anyLong());
    }

    @Test
    @DisplayName("given admin user and not existing company when unlink all users by user id then throw resource not found exception")
    void givenNotExistingCompany_whenUnlinkAllByUserId_thenThrowException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(isAdminUser.execute(any(User.class))).thenReturn(true);
        when(isUserExists.execute(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyUserService.unlinkAllByUserId(adminUser, 2L));

        verify(companyUserProvider, never()).unlinkAllByUserId(anyLong());
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