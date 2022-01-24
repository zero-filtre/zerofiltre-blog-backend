package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.mail.javamail.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserMailNotificationProviderIT {

    UserMailNotificationProvider userMailNotificationProvider;
    RegistrationCompleteListener registrationCompleteListener;


    @MockBean
    VerificationTokenProvider verificationTokenProvider;


    @MockBean
    MessageSource messageSource;


    @MockBean
    JavaMailSender javaMailSender;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    User user = new User();


    @BeforeEach
    void setUp() {
        userMailNotificationProvider = new UserMailNotificationProvider(applicationEventPublisher);
        registrationCompleteListener = new RegistrationCompleteListener(verificationTokenProvider, messageSource, javaMailSender);

    }

    @Test
    void onNotifyRegistrationComplete_ListenerIsTriggered() {
        //ARRANGE
        RegistrationCompleteEvent event = new RegistrationCompleteEvent(
                "appUrl",
                Locale.FRANCE,
                user,
                false);


        //ACT
        userMailNotificationProvider.notifyRegistrationComplete(event);

        //ASSERT
        verify(applicationEventPublisher,times(1)).publishEvent(any());

    }

}