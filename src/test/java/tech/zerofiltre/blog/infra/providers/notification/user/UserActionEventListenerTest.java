package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.context.*;
import org.thymeleaf.*;
import org.thymeleaf.context.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

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
        doNothing().when(mailSender).send(any());

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
        verify(mailSender, times(1)).send(any());
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
        verify(mailSender, times(1)).send(any());
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
        verify(mailSender, times(1)).send(any());
    }
}