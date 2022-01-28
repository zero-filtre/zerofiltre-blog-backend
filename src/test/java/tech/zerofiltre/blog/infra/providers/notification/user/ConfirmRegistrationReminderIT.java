package tech.zerofiltre.blog.infra.providers.notification.user;

import org.awaitility.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.*;

import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ConfirmRegistrationReminderIT {

    @SpyBean
    ConfirmRegistrationReminder confirmRegistrationReminder;

    @Test
    @DisplayName("Given an execution rate of 100ms, remindConfirmRegistration must be scheduled at least 9 times in 1s")
    void remindConfirmRegistration_mustStartAtLeast9Times_within1s() {

        await().atMost(Duration.ONE_SECOND)
                .untilAsserted(() -> verify(confirmRegistrationReminder, atLeast(9)).remindConfirmRegistration());
    }
}