package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.notification.user.model.UserActionApplicationEvent;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActionEventListenerTest {

    @Mock
    MessageSource messageSource;
    @Mock
    ZerofiltreEmailSender mailSender;
    @Mock
    ITemplateEngine emailTemplateEngine;

    UserActionEventListener userActionEventListener;


    @BeforeEach
    void setUp() {
        userActionEventListener = new UserActionEventListener(messageSource, mailSender, emailTemplateEngine);

        when(emailTemplateEngine.process(anyString(), any(Context.class))).thenReturn("<a href=zerofiltre.tech>Home</a>");
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), anyBoolean());

    }

    @Test
    void onRegistrationComplete_Notify_withoutRegeneratingToken() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        user.setEmail("test.user@zerofiltre.tech");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl", "",
                Action.REGISTRATION_COMPLETE);

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        verify(mailSender, times(1)).send(any(), anyBoolean());
    }

    @Test
    void onResendRegistrationComplete_Notify() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        user.setEmail("test.user@zerofiltre.tech");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl", "",
                Action.REGISTRATION_COMPLETE);

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        verify(mailSender, times(1)).send(any(), anyBoolean());
    }

    @Test
    void onResetPassword_Notify() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        user.setEmail("test.user@zerofiltre.tech");
        UserActionApplicationEvent event = new UserActionApplicationEvent(
                user,
                Locale.FRANCE,
                "appUrl", "",
                Action.PASSWORD_RESET
        );

        //ACT
        userActionEventListener.onApplicationEvent(event);

        //ASSERT
        verify(mailSender, times(1)).send(any(), anyBoolean());
    }
}