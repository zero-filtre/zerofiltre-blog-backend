package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class NotifyRegistrationCompleteTest {

    public static final String APP_URL = "appUrl";
    NotifyRegistrationComplete notifyRegistrationComplete;

    @MockBean
    UserNotificationProvider userNotificationProvider;


    @BeforeEach
    void setUp() {
        notifyRegistrationComplete = new NotifyRegistrationComplete(userNotificationProvider);
    }

    @Test
    void mustBuildTheNotificationEvent_ThenNotify() {
        //ARRANGE
        doNothing().when(userNotificationProvider).notify(any());

        //ACT
        notifyRegistrationComplete.execute(new User(), APP_URL, Locale.FRANCE);

        //ASSERT
        ArgumentCaptor<UserActionEvent> captor = ArgumentCaptor.forClass(UserActionEvent.class);

        verify(userNotificationProvider, times(1)).notify(captor.capture());
        UserActionEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);
        assertThat(event.getAction()).isEqualTo(Action.REGISTRATION_COMPLETE);
    }
}