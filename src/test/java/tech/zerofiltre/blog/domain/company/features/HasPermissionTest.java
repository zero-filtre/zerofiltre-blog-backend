package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class HasPermissionTest {

    private static User adminUser;
    private static User userWithUserRole;

    private HasPermission hasPermission;

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
        hasPermission = new HasPermission();
    }

    @Test
    @DisplayName("given a admin user and role admin when hasPermission then return true")
    void givenAdminUserAndRoleAdmin_whenHasPermission_thenReturnTrue() throws ForbiddenActionException {
        //WHEN
        boolean response = hasPermission.execute(adminUser, false, LinkCompanyUser.Role.ADMIN);

        //THEN
        assertThat(response).isTrue();
    }

    @DisplayName("given admin user and role editor or viewer when hasPermission then return true")
    @ParameterizedTest(name = "[{index}] admin company user - user role added: {0}")
    @MethodSource("editorAndViewerRoleProvider")
    void givenAdminUserAndRoleEditorOrViewer_whenHasPermission_thenReturnTrue(LinkCompanyUser.Role role) throws ForbiddenActionException {
        //WHEN
        boolean response = hasPermission.execute(adminUser, false, role);

        //THEN
        assertThat(response).isTrue();
    }

    @DisplayName("given admin company user when hasPermission then return true")
    @ParameterizedTest(name = "[{index}] admin company user - user role added: {0}")
    @MethodSource("editorAndViewerRoleProvider")
    void givenAdminCompanyUser_whenHasPermission_thenReturnTrue(LinkCompanyUser.Role role) throws ForbiddenActionException {
        //WHEN
        boolean response = hasPermission.execute(userWithUserRole, true, role);

        //THEN
        assertThat(response).isTrue();
    }

    static Stream<Arguments> editorAndViewerRoleProvider() {
        return Stream.of(
                arguments(LinkCompanyUser.Role.EDITOR),
                arguments(LinkCompanyUser.Role.VIEWER)
        );
    }

    @Test
    @DisplayName("given not admin user when hasPermission then throw ForbiddenActionException")
    void givenNotAdminUser_whenHasPermission_thenThrowForbiddenActionException() {
        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> hasPermission.execute(userWithUserRole, false, LinkCompanyUser.Role.ADMIN));
    }

    @DisplayName("given user when hasPermission then throw ForbiddenActionException")
    @ParameterizedTest(name = "[{index}] user with user role - user role added: {0}")
    @MethodSource("editorAndViewerRoleProvider")
    void givenUser_whenHasPermission_thenThrowForbiddenActionException(LinkCompanyUser.Role role) {
        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> hasPermission.execute(userWithUserRole, false, role));
    }

}