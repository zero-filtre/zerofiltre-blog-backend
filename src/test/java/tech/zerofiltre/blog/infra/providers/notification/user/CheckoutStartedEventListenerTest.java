package tech.zerofiltre.blog.infra.providers.notification.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.CheckoutStartedEvent;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

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
        lenient().doNothing().when(mailSender).send(any(), anyBoolean());

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
        verify(mailSender, Mockito.times(0)).send(any(), anyBoolean());

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
        verify(mailSender, Mockito.times(2)).send(any(), anyBoolean());
        Assertions.assertThat(eventListener.getEvents().size()).isOne();


    }

}