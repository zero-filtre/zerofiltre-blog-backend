package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class IsAdminUserTest {

    private IsAdminUser isAdminUser;

    @BeforeEach
    void init() {
        isAdminUser = new IsAdminUser();
    }

    @Test
    @DisplayName("given admin user when check if user is admin then return true")
    void givenAdminUser_whenCheckIfUserIsAdmin_thenReturnTrue() throws ForbiddenActionException {
        //GIVEN
        User user = new User();
        user.getRoles().add("ROLE_ADMIN");

        //WHEN
        boolean response = isAdminUser.execute(user);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given user with role user when check if user is admin then throw forbidden action exception")
    void givenUserWithRoleUser_whenCheckIfUserIsAdmin_thenThrowForbiddenActionException() {
        //GIVEN
        User user = new User();
        user.getRoles().add("ROLE_USER");

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> isAdminUser.execute(user));
    }

}