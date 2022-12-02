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

import java.time.*;
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

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);


    @BeforeEach
    void setUp() {
        userActionEventListener = new UserActionEventListener(messageSource, mailSender, tokenProvider);
    }

    @Test
    void onRegistrationComplete_Notify_withoutRegeneratingToken() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl", "",
                Action.REGISTRATION_COMPLETE);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        verify(tokenProvider, times(0)).generate(any());
        String firstName = StringUtils.capitalize(user.getFullName());
        verify(messageSource, times(1)).getMessage("message.registration.success.content", new Object[]{firstName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }

    @Test
    void onResendRegistrationComplete_Notify() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl", "",
                Action.REGISTRATION_COMPLETE);
        when(tokenProvider.generate(any())).thenReturn(new VerificationToken(user, "", expiryDate));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        String firstName = StringUtils.capitalize(user.getFullName());
        verify(messageSource, times(1)).getMessage("message.registration.success.content", new Object[]{firstName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }

    @Test
    void onResetPassword_Notify() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl", "",
                Action.PASSWORD_RESET
        );
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        String firstName = StringUtils.capitalize(user.getFullName());
        verify(messageSource, times(1)).getMessage("message.reset.content", new Object[]{firstName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }
}