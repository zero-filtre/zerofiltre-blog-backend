package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class IsCompanyAdminOrCompanyEditorTest {

    private static User userWithUserRole;
    private static LinkCompanyUser linkCompanyUserRoleAdmin;
    private static LinkCompanyUser linkCompanyUserRoleEditor;
    private static LinkCompanyUser linkCompanyUserRoleViewer;

    IsCompanyAdminOrCompanyEditor isCompanyAdminOrCompanyEditor;

    @Mock
    CompanyUserProvider companyUserProvider;

    @BeforeAll
    static void setup() {
        userWithUserRole = new User();
        userWithUserRole.setId(1L);
        userWithUserRole.getRoles().add("ROLE_USER");

        linkCompanyUserRoleAdmin = new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.ADMIN);
        linkCompanyUserRoleEditor = new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.EDITOR);
        linkCompanyUserRoleViewer = new LinkCompanyUser(1L, 1L, LinkCompanyUser.Role.VIEWER);
    }

    @BeforeEach
    void init() {
        isCompanyAdminOrCompanyEditor = new IsCompanyAdminOrCompanyEditor(companyUserProvider);
    }

    @DisplayName("given user with user role and good company role when execute then return true")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("userWithGoodRoleProvider")
    void givenUserWithUserRoleAndGoodCompanyRole_whenExecute_thenReturnTrue(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(companyUser);

        //WHEN
        boolean response = isCompanyAdminOrCompanyEditor.execute(user, 1L);

        //THEN
        assertThat(response).isTrue();
    }

    static Stream<Arguments> userWithGoodRoleProvider() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleAdmin), LinkCompanyUser.Role.ADMIN),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR)
        );
    }

    @DisplayName("given user with bad role company when execute then throw ForbiddenActionException")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("userWithBadRoleProvider")
    void givenUserWithBadRoleCompany_whenExecute_thenThrowForbiddenActionException(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) throws ForbiddenActionException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(companyUser);

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> isCompanyAdminOrCompanyEditor.execute(user, 1L));
    }

    static Stream<Arguments> userWithBadRoleProvider() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER),
                arguments(userWithUserRole, "user with user role", Optional.empty(), LinkCompanyUser.Role.ADMIN)
        );
    }

}