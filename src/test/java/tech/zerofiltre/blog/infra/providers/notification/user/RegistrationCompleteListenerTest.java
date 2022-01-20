package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RegistrationCompleteListenerTest {

    @MockBean
    VerificationTokenProvider verificationTokenProvider;
    @MockBean
    MessageSource messageSource;
    @MockBean
    JavaMailSender mailSender;

    RegistrationCompleteListener registrationCompleteListener;

    @BeforeEach
    void setUp() {
        registrationCompleteListener = new RegistrationCompleteListener(verificationTokenProvider, messageSource, mailSender);
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
        when(verificationTokenProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        //ACT
        registrationCompleteListener.onApplicationEvent(event);

        //ASSERT
        verify(verificationTokenProvider, times(1)).save(any());
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        verify(messageSource, times(1)).getMessage("message.registration.success", new Object[]{firstName, lastName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
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
        when(verificationTokenProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        when(verificationTokenProvider.ofUser(user)).thenReturn(Optional.of(new VerificationToken(user, "token")));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        //ACT
        registrationCompleteListener.onApplicationEvent(event);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofUser(user);
        verify(verificationTokenProvider, times(1)).save(any());
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        verify(messageSource, times(1)).getMessage("message.registration.success", new Object[]{firstName, lastName}, Locale.FRANCE);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}