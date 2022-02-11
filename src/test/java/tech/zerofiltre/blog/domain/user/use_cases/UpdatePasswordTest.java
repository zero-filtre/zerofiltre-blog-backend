package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UpdatePasswordTest {

    private UpdatePassword updatePassword;

    @MockBean
    private UserProvider userProvider;

    @MockBean
    private PasswordVerifierProvider passwordVerifierProvider;


    @BeforeEach
    void setUp() {
        updatePassword = new UpdatePassword(userProvider, passwordVerifierProvider);
    }

    @Test
    void updatePassword_isOkOnValidInput() {
        //ARRANGE
        User user = new User();
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));
        when(passwordVerifierProvider.isValid(any(), any())).thenReturn(true);

        //ACT
        assertThatNoException().isThrownBy(() -> updatePassword.execute("", "", ""));


        //ASSERT
        verify(passwordVerifierProvider, times(1)).isValid(user, "");
    }

    @Test
    void updatePassword_throws_UserNotFoundException_OnMissingUser() {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> updatePassword.execute("", "", ""));

    }

    @Test
    void updatePassword_throws_InvalidPasswordException_OnIncorrectPassword() {
        //ARRANGE
        User user = new User();
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));
        when(passwordVerifierProvider.isValid(any(), any())).thenReturn(false);

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidPasswordException.class).isThrownBy(() -> updatePassword.execute("", "", ""));

    }


}