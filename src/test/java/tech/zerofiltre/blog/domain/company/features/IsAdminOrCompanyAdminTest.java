package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsAdminOrCompanyAdminTest {

    private static User adminUser;
    private static User userWithUserRole;
    private static LinkCompanyUser linkCompanyUserRoleAdmin;
    private static LinkCompanyUser linkCompanyUserRoleEditor;
    private static LinkCompanyUser linkCompanyUserRoleViewer;

    IsAdminOrCompanyAdmin isAdminOrCompanyAdmin;

    @Mock
    CompanyUserProvider companyUserProvider;

    @BeforeAll
    static void setup() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.getRoles().add("ROLE_ADMIN");

        userWithUserRole = new User();
        userWithUserRole.setId(1L);
        userWithUserRole.getRoles().add("ROLE_USER");

        linkCompanyUserRoleAdmin = new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.ADMIN);
        linkCompanyUserRoleEditor = new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.EDITOR);
        linkCompanyUserRoleViewer = new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.VIEWER);
    }

    @BeforeEach
    void init() {
        isAdminOrCompanyAdmin = new IsAdminOrCompanyAdmin(companyUserProvider);
    }

    @Test
    @DisplayName("given admin connected user when execute then return true")
    void givenAdminConnectedUser_whenExecute_thenReturnTrue() throws ForbiddenActionException {
        //WHEN
        boolean response = isAdminOrCompanyAdmin.execute(adminUser, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given user with role company admin when execute then return true")
    void givenUserWithRoleCompanyAdmin_whenExecute_thenReturnTrue() throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyUserRoleAdmin));

        //WHEN
        boolean response = isAdminOrCompanyAdmin.execute(userWithUserRole, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    @DisplayName("given user with user role and bad company role when execute then throw ForbiddenActionException")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("userWithBadRoleProvider")
    void givenUserWithUserRoleAndBadCompanyRole_whenExecute_thenThrowForbiddenActionException(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(companyUser);

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> isAdminOrCompanyAdmin.execute(user, 1L));
    }

    static Stream<Arguments> userWithBadRoleProvider() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER),
                arguments(userWithUserRole, "user with user role", Optional.empty(), LinkCompanyUser.Role.ADMIN)
        );
    }

}