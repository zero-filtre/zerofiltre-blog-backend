package tech.zerofiltre.blog.infra.providers.notification.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.context.*;
import org.springframework.test.util.*;
import org.thymeleaf.*;
import org.thymeleaf.context.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutStartedEventListenerTest {

    @Mock
    MessageSource messageSource;
    @Mock
    ZerofiltreEmailSender mailSender;
    @Mock
    ITemplateEngine emailTemplateEngine;

    @Mock
    InfraProperties infraProperties;

    CheckoutStartedEventListener eventListener;


    @BeforeEach
    void setUp() {

        lenient().when(infraProperties.getCheckoutReminderDelayMs()).thenReturn(86400000L);
        lenient().when(infraProperties.getCheckoutReminderCheckFrequencyMs()).thenReturn(3600000L);

        eventListener = new CheckoutStartedEventListener(messageSource, mailSender, emailTemplateEngine, infraProperties);

        lenient().when(emailTemplateEngine.process(anyString(), any(Context.class))).thenReturn("<a href=zerofiltre.tech>Home</a>");
        lenient().when(messageSource.getMessage(any(), any(), any())).thenReturn("message");
        lenient().doNothing().when(mailSender).send(any());

    }

    @Test
    void onCheckout_addEvent_ForFutureNotification() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        CheckoutStartedEvent event = new CheckoutStartedEvent(
                user,
                Locale.FRANCE,
                "appUrl");

        //ACT
        eventListener.onApplicationEvent(event);

        //ASSERT
        Assertions.assertThat(eventListener.getEvents().size()).isOne();
    }

    @Test
    void doNotHandleEvent_ifItIs_NotYet_TheTime() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        CheckoutStartedEvent event = new CheckoutStartedEvent(
                user,
                Locale.FRANCE,
                "appUrl");

        long _5HoursAgo = System.currentTimeMillis() - 5 * 3600 * 1000;
        ReflectionTestUtils.setField(event, "timestamp", _5HoursAgo);

        //ACT
        eventListener.onApplicationEvent(event);
        eventListener.handleEventIfNeeded();

        //ASSERT
        verify(mailSender, Mockito.times(0)).send(any());

    }

    @Test
    @Disabled("Random Failure")
    void handleEvent_ifIts_Time() {
        //ARRANGE
        User user = new User();
        user.setFullName("tester");
        user.setEmail("test.user@zerofiltre.tech");
        CheckoutStartedEvent event = new CheckoutStartedEvent(
                user,
                Locale.FRANCE,
                "appUrl");

        CheckoutStartedEvent secondEvent = new CheckoutStartedEvent(
                user,
                Locale.FRANCE,
                "appUrl");

        CheckoutStartedEvent thirdEvent = new CheckoutStartedEvent(
                user,
                Locale.FRANCE,
                "appUrl");

        long _25HoursAgo = System.currentTimeMillis() - 25 * 3600 * 1000;
        long _5HoursAgo = System.currentTimeMillis() - 5 * 3600 * 1000;

        ReflectionTestUtils.setField(event, "timestamp", _25HoursAgo);
        ReflectionTestUtils.setField(secondEvent, "timestamp", _25HoursAgo);
        ReflectionTestUtils.setField(thirdEvent, "timestamp", _5HoursAgo);

        //ACT
        eventListener.onApplicationEvent(event);
        eventListener.onApplicationEvent(secondEvent);
        eventListener.onApplicationEvent(thirdEvent);
        eventListener.handleEventIfNeeded();

        //ASSERT
        verify(mailSender, Mockito.times(2)).send(any());
        Assertions.assertThat(eventListener.getEvents().size()).isOne();


    }

}