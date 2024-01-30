package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.model.Event;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.infra.InfraProperties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeProviderTest {

    @Mock
    Event event;

    @Mock
    InfraProperties infraProperties;
    @Mock
    SessionEventHandler sessionEventHandler;
    @Mock
    InvoiceEventHandler invoiceEventHandler;
    @Mock
    UserProvider userProvider;
    @Mock
    MetricsProvider metricsProvider;
    @Mock
    UserNotificationProvider userNotificationProvider;

    @Mock
    ChargeRequest chargeRequestVM;

    @Mock
    Course course;

    StripeProvider stripeProvider;

    @BeforeEach
    void init() {
        stripeProvider = new StripeProvider(infraProperties, sessionEventHandler, invoiceEventHandler, userProvider, metricsProvider, userNotificationProvider);
        lenient().doNothing().when(userNotificationProvider).notify(any());
    }

    @Test
    void returnsProPlanPrice_WhenModeIsSubscription_AndProPlan_AndRecurringIntervalIsMonth() {
        // Arrange
        when(chargeRequestVM.isProPlan()).thenReturn(true);
        when(chargeRequestVM.getRecurringInterval()).thenReturn("month");
        when(infraProperties.getProPlanPriceId()).thenReturn("proPlanPriceId");
        when(course.isMentored()).thenReturn(false);


        // Act
        String result = stripeProvider.getProductPrice(course, chargeRequestVM, SessionCreateParams.Mode.SUBSCRIPTION);

        // Assert
        assertThat(infraProperties.getProPlanPriceId()).isEqualTo(result);
    }

    @Test
    void returnsProPlanYearlyPrice_WhenModeIsSubscription_AndProPlan_AndRecurringIntervalIsYear() {
        // Arrange
        when(chargeRequestVM.isProPlan()).thenReturn(true);
        when(chargeRequestVM.getRecurringInterval()).thenReturn("year");
        when(infraProperties.getProPlanYearlyPriceId()).thenReturn("proPlanYearlyPriceId");
        when(course.isMentored()).thenReturn(false);

        // Act
        String result = stripeProvider.getProductPrice(course, chargeRequestVM, SessionCreateParams.Mode.SUBSCRIPTION);

        // Assert
        assertThat(infraProperties.getProPlanYearlyPriceId()).isEqualTo(result);
    }

    @Test
    void returnsProductPrice_WhenModeIsSubscription_AndProPlanAndMentored() {
        // Arrange
        when(chargeRequestVM.isProPlan()).thenReturn(true);
        when(course.isMentored()).thenReturn(true);
        when(course.getPrice()).thenReturn(50L);

        // Act
        String result = stripeProvider.getProductPrice(course, chargeRequestVM, SessionCreateParams.Mode.SUBSCRIPTION);

        // Assert
        assertThat("50").isEqualTo(result);
    }

    @Test
    void returnsProductPrice_WhenModeIsSubscription_AndNotProPlanAndMentored() {
        // Arrange
        when(chargeRequestVM.isProPlan()).thenReturn(false);
        when(course.isMentored()).thenReturn(true);
        when(course.getPrice()).thenReturn(50L);

        // Act
        String result = stripeProvider.getProductPrice(course, chargeRequestVM, SessionCreateParams.Mode.SUBSCRIPTION);

        // Assert
        assertThat("50").isEqualTo(result);
    }

    @Test
    void returnsThirdOfProductPrice_WhenModeIsSubscription_AndNotProPlan_AndNotMentored() {
        // Arrange
        when(chargeRequestVM.isProPlan()).thenReturn(false);
        when(course.isMentored()).thenReturn(false);
        when(course.getPrice()).thenReturn(60L);

        // Act
        String result = stripeProvider.getProductPrice(course, chargeRequestVM, SessionCreateParams.Mode.SUBSCRIPTION);
        // result = price / 3 + 1

        // Assert
        assertThat("21").isEqualTo(result);
    }

    @Test
    void returnsProductPrice_WhenModeIsNotSubscription() {
        // Arrange
        when(course.getPrice()).thenReturn(60L);

        // Act
        String result = stripeProvider.getProductPrice(course, chargeRequestVM, SessionCreateParams.Mode.PAYMENT);

        // Assert
        assertThat("60").isEqualTo(result);
    }
}