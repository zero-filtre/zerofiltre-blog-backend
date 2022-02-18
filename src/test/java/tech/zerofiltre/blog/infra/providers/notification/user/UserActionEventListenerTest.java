package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserActionEventListenerTest {

    @MockBean
    VerificationTokenProvider tokenProvider;
    @MockBean
    MessageSource messageSource;
    @MockBean
    BlogEmailSender mailSender;

    UserActionEventListener userActionEventListener;

    @BeforeEach
    void setUp() {
        userActionEventListener = new UserActionEventListener(messageSource, mailSender, tokenProvider);
    }

    @Test
    void onRegistrationComplete_CreateToken_ThenNotify() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl",
                Action.REGISTRATION_COMPLETE);
        when(tokenProvider.generate(any())).thenReturn(new VerificationToken(user, ""));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        verify(tokenProvider, times(1)).generate(any());
        String firstName = StringUtils.capitalize(user.getFullName());
        verify(messageSource, times(1)).getMessage("message.registration.success.content", new Object[]{firstName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }

    @Test
    void onResendRegistrationComplete_CheckThenUpdateToken_ThenNotify() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl",
                Action.REGISTRATION_COMPLETE);
        when(tokenProvider.generate(any())).thenReturn(new VerificationToken(user, ""));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        verify(tokenProvider, times(1)).generate(user);
        String firstName = StringUtils.capitalize(user.getFullName());
        verify(messageSource, times(1)).getMessage("message.registration.success.content", new Object[]{firstName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }

    @Test
    void onResetPassword_CreateToken_ThenNotify() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl",
                Action.PASSWORD_RESET
        );
        when(tokenProvider.generate(any())).thenReturn(new VerificationToken(user, ""));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        verify(tokenProvider, times(1)).generate(user);
        String firstName = StringUtils.capitalize(user.getFullName());
        verify(messageSource, times(1)).getMessage("message.reset.content", new Object[]{firstName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }
}