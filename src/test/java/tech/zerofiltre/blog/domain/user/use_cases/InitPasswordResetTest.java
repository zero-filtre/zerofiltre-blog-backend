package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
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

    @MockBean
    private LoggerProvider loggerProvider;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);


    InitPasswordReset initPasswordReset;

    @BeforeEach
    void setUp() {
        initPasswordReset = new InitPasswordReset(userProvider, userNotificationProvider, tokenProvider, loggerProvider);
        when(tokenProvider.generate(any(),anyLong())).thenAnswer(invocationOnMock -> new VerificationToken(invocationOnMock.getArgument(0), "",expiryDate));

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

    @Test
    void mustCheckUser_ThendDoNotNotify_whenSocialAccount() throws UserNotFoundException {
        //ARRANGE
        User user = new User();
        user.setLoginFrom(SocialLink.Platform.GITHUB);
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));
        doNothing().when(userNotificationProvider).notify(any());

        //ACT
        initPasswordReset.execute("email", "appUrl", Locale.FRANCE);

        //ASSERT
        verify(loggerProvider).log(any(LogEntry.class));
        verify(userNotificationProvider, times(1)).notify(any());
    }
}