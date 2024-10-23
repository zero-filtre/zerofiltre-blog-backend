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
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsLinkCompanyUserTest {

    private static User userWithUserRole;
    private static LinkCompanyUser linkCompanyUserRoleAdmin;
    private static LinkCompanyUser linkCompanyUserRoleEditor;
    private static LinkCompanyUser linkCompanyUserRoleViewer;

    IsCompanyUser isCompanyUser;

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
        isCompanyUser = new IsCompanyUser(companyUserProvider);
    }

    @DisplayName("given company user when execute then return true")
    @ParameterizedTest(name = "[{index}] connected: {1} - company role: {3}")
    @MethodSource("companyUserProvider")
    void givenCompanyUser_whenExecute_thenReturnTrue(User user, String userInfo, Optional<LinkCompanyUser> companyUser, LinkCompanyUser.Role role) throws ResourceNotFoundException {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(companyUser);

        //WHEN
        boolean response = isCompanyUser.execute(1L, user.getId());

        //THEN
        assertThat(response).isTrue();
    }

    static Stream<Arguments> companyUserProvider() {
        return Stream.of(
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleAdmin), LinkCompanyUser.Role.ADMIN),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleEditor), LinkCompanyUser.Role.EDITOR),
                arguments(userWithUserRole, "user with user role", Optional.of(linkCompanyUserRoleViewer), LinkCompanyUser.Role.VIEWER)
        );
    }

    @Test
    @DisplayName("given non company user when execute then throw ResourceNotFoundException")
    void givenNonCompanyUser_whenExecute_thenThrowException() {
        //GIVEN
        when(companyUserProvider.findByCompanyIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> isCompanyUser.execute(1L, 1L));
    }

}