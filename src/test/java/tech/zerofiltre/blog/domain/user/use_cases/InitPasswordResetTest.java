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
class InitPasswordResetTest {

    @MockBean
    UserNotificationProvider userNotificationProvider;

    @MockBean
    UserProvider userProvider;

    @MockBean
    VerificationTokenProvider tokenProvider;

    InitPasswordReset initPasswordReset;

    @BeforeEach
    void setUp() {
        initPasswordReset = new InitPasswordReset(userProvider, userNotificationProvider, tokenProvider);
        when(tokenProvider.generate(any())).thenAnswer(invocationOnMock -> new VerificationToken(invocationOnMock.getArgument(0), ""));

    }

    @Test
    void mustCheckUser_ThenNotify() throws UserNotFoundException {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));
        doNothing().when(userNotificationProvider).notify(any());

        //ACT
        initPasswordReset.execute("email", "appUrl", Locale.FRANCE);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userNotificationProvider, times(1)).notify(any());

    }

    @Test
    void onUserNotFound_ThrowUserNotFoundException() {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> initPasswordReset.execute("email", "appUrl", Locale.FRANCE));

    }
}