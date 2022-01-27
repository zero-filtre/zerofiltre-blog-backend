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
    void notifyRegistrationComplete_mustConstructEvent_ThenPublish_withProperAction() {
        //ARRANGE
        doNothing().when(eventPublisher).publishEvent(any());

        //ACT
        user.setLastName(LAST_NAME);
        userMailNotificationProvider.notify(new UserActionEvent(
                APP_URL, Locale.FRANCE, user, Action.REGISTRATION_COMPLETE));

        //ASSERT
        ArgumentCaptor<UserActionApplicationEvent> captor = ArgumentCaptor.forClass(UserActionApplicationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        UserActionApplicationEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getAction()).isEqualTo(Action.REGISTRATION_COMPLETE);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);
        assertThat(event.getUser().getLastName()).isEqualTo(LAST_NAME);
    }

    @Test
    void notifyResetPassword_mustConstructEvent_ThenPublish_withProperAction() {
        //ARRANGE
        doNothing().when(eventPublisher).publishEvent(any());

        //ACT
        user.setLastName(LAST_NAME);
        userMailNotificationProvider.notify(new UserActionEvent(
                APP_URL, Locale.FRANCE, user, Action.PASSWORD_RESET));

        //ASSERT
        ArgumentCaptor<UserActionApplicationEvent> captor = ArgumentCaptor.forClass(UserActionApplicationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        UserActionApplicationEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getAction()).isEqualTo(Action.PASSWORD_RESET);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);
        assertThat(event.getUser().getLastName()).isEqualTo(LAST_NAME);

    }
}