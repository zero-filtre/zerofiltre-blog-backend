package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RegistrationCompleteListenerTest {

    @MockBean
    VerificationTokenManager tokenManager;
    @MockBean
    MessageSource messageSource;
    @MockBean
    BlogEmailSender mailSender;

    RegistrationCompleteListener registrationCompleteListener;

    @BeforeEach
    void setUp() {
        registrationCompleteListener = new RegistrationCompleteListener(messageSource, mailSender, tokenManager);
    }

    @Test
    void onRegistrationComplete_CreateToken_ThenNotify() {
        //ARRANGE
        User user = new User();
        user.setLastName("tester");
        user.setFirstName("tester");
        OnRegistrationCompleteEvent event = new OnRegistrationCompleteEvent(
                user,
                Locale.FRANCE,
                "appUrl",
                false
        );
        when(tokenManager.generateToken(any())).thenReturn("token");
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        registrationCompleteListener.onApplicationEvent(event);

        //ASSERT
        verify(tokenManager, times(1)).generateToken(any());
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        verify(messageSource, times(1)).getMessage("message.registration.success", new Object[]{firstName, lastName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }

    @Test
    void onResendRegistrationComplete_CheckThenUpdateToken_ThenNotify() {
        //ARRANGE
        User user = new User();
        user.setLastName("tester");
        user.setFirstName("tester");
        OnRegistrationCompleteEvent event = new OnRegistrationCompleteEvent(
                user,
                Locale.FRANCE,
                "appUrl",
                true
        );
        when(tokenManager.updateToken(any())).thenReturn("token");
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(), any(), any());

        //ACT
        registrationCompleteListener.onApplicationEvent(event);

        //ASSERT
        verify(tokenManager, times(1)).updateToken(user);
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        verify(messageSource, times(1)).getMessage("message.registration.success", new Object[]{firstName, lastName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(), any(), any());
    }
}