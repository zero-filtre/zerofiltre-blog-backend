package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserMailNotificationProviderTest {

    public static final String APP_URL = "appUrl";
    public static final String LAST_NAME = "last";
    UserMailNotificationProvider userMailNotificationProvider;

    @MockBean
    ApplicationEventPublisher eventPublisher;

    User user;


    @BeforeEach
    void setUp() {
        user = new User();
        userMailNotificationProvider = new UserMailNotificationProvider(eventPublisher);
    }

    @Test
    void notifyRegistrationComplete_mustConstructEvent_ThenPublish() {
        //ARRANGE
        doNothing().when(eventPublisher).publishEvent(any());

        //ACT
        user.setLastName(LAST_NAME);
        userMailNotificationProvider.notify(new RegistrationCompleteEvent(
                APP_URL, Locale.FRANCE, user, true));

        //ASSERT
        ArgumentCaptor<OnRegistrationCompleteEvent> captor = ArgumentCaptor.forClass(OnRegistrationCompleteEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        OnRegistrationCompleteEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);
        assertThat(event.getUser().getLastName()).isEqualTo(LAST_NAME);
        assertThat(event.isRepeated()).isTrue();

    }

    @Test
    void notifyResetPassword_mustConstructEvent_ThenPublish() {
        //ARRANGE
        doNothing().when(eventPublisher).publishEvent(any());

        //ACT
        user.setLastName(LAST_NAME);
        userMailNotificationProvider.notify(new ResetPasswordEvent(
                APP_URL, Locale.FRANCE, user));

        //ASSERT
        ArgumentCaptor<OnResetPasswordEvent> captor = ArgumentCaptor.forClass(OnResetPasswordEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        OnResetPasswordEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);
        assertThat(event.getUser().getLastName()).isEqualTo(LAST_NAME);

    }

    @Test
    void onNotifyRegistrationComplete_PublishWithProperEvent() {
        //ARRANGE
        RegistrationCompleteEvent event = new RegistrationCompleteEvent(
                "appUrl",
                Locale.FRANCE,
                user,
                false);


        //ACT
        userMailNotificationProvider.notify(event);

        //ASSERT
        verify(eventPublisher, times(1)).publishEvent(any(OnRegistrationCompleteEvent.class));
    }

    @Test
    void onNotifyResetPassword_PublishWithProperEvent() {
        //ARRANGE
        user = new User();
        ResetPasswordEvent event = new ResetPasswordEvent(
                "appUrl",
                Locale.FRANCE,
                user
        );


        //ACT
        userMailNotificationProvider.notify(event);

        //ASSERT
        verify(eventPublisher, times(1)).publishEvent(any(OnResetPasswordEvent.class));

    }
}