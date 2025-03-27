package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.api.notchpay.NotchPayProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobilePaymentReminderTest {

    @Mock
    UserProvider userProvider;
    @Mock
    ZerofiltreEmailSender emailSender;
    @Mock
    NotchPayProvider notchPayProvider;

    @InjectMocks
    MobilePaymentReminder paymentReminder;

    User user;

    @BeforeEach
    void initialize() {
        user = new User();
        user.setId(1L);
        user.setPaymentEmail("test@test.com");
        user.setPlan(User.Plan.PRO);


    }

    @Test
    @DisplayName("User stays pro if there is a non expired payment")
    void userStaysPro_ifHasAValidPayment() throws ZerofiltreException {
        //Given
        LocalDateTime now = LocalDateTime.now();

        Payment expiredMonthlyPayment = new Payment();
        expiredMonthlyPayment.setAt(now.minusMonths(2));
        expiredMonthlyPayment.setRecurringInterval(Payment.MONTH);
        expiredMonthlyPayment.setUser(user);

        Payment validPayment = new Payment();
        validPayment.setAt(now);
        validPayment.setRecurringInterval(Payment.MONTH);
        validPayment.setUser(user);

        when(notchPayProvider.payments()).thenReturn(List.of(expiredMonthlyPayment, validPayment));

        //when
        paymentReminder.sendStats();

        //then
        verify(notchPayProvider, never()).createSession(any(), any(), any());
        verify(userProvider, never()).save(any());

    }

    @Test
    @DisplayName("User gets basic if all payments are expired")
    void userGetsBasic_ifHasNoValidPayment() throws ZerofiltreException {
        //Given
        LocalDateTime now = LocalDateTime.now();

        Payment expiredMonthlyPayment = new Payment();
        expiredMonthlyPayment.setAt(now.minusMonths(2));
        expiredMonthlyPayment.setRecurringInterval(Payment.MONTH);
        expiredMonthlyPayment.setUser(user);

        Payment expiredYearlyPayment = new Payment();
        expiredYearlyPayment.setAt(now.minusYears(2));
        expiredYearlyPayment.setRecurringInterval(Payment.YEAR);
        expiredYearlyPayment.setUser(user);

        when(notchPayProvider.payments()).thenReturn(List.of(expiredMonthlyPayment, expiredYearlyPayment));

        //when
        paymentReminder.sendStats();

        //then
        verify(notchPayProvider, times(1)).createSession(any(), any(), any());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userProvider, times(1)).save(captor.capture());
        assertThat(captor.getValue().getPlan()).isEqualTo(User.Plan.BASIC);
        assertThat(captor.getValue().getId()).isEqualTo(user.getId());

    }

    @Test
    @DisplayName("User gets notified for all almost expired payments")
    void userGetsNotified_forAllAlmostExpiredPayments() throws ZerofiltreException {
        //Given
        LocalDateTime now = LocalDateTime.now();

        Payment almostExpiredMonthlyPayment = new Payment();
        almostExpiredMonthlyPayment.setAt(now.minusMonths(1).plusDays(2));
        almostExpiredMonthlyPayment.setRecurringInterval(Payment.MONTH);
        almostExpiredMonthlyPayment.setUser(user);

        Payment almostExpiredYearlyPayment = new Payment();
        almostExpiredYearlyPayment.setAt(now.minusYears(1).plusDays(2));
        almostExpiredYearlyPayment.setRecurringInterval(Payment.YEAR);
        almostExpiredYearlyPayment.setUser(user);

        when(notchPayProvider.payments()).thenReturn(List.of(almostExpiredMonthlyPayment, almostExpiredYearlyPayment));

        //when
        paymentReminder.sendStats();

        //then
        verify(notchPayProvider, times(2)).createSession(any(), any(), any());
        verify(userProvider, never()).save(any());
        verify(emailSender, times(2)).send(any(), anyBoolean());

    }

    @Test
    @DisplayName("User does not get notified if has a valid payment")
    void userNotNotified_IfHasAValidPayment() throws ZerofiltreException {
        //Given
        LocalDateTime now = LocalDateTime.now();

        Payment almostExpiredMonthlyPayment = new Payment();
        almostExpiredMonthlyPayment.setAt(now.minusMonths(1).plusDays(10));
        almostExpiredMonthlyPayment.setRecurringInterval(Payment.MONTH);
        almostExpiredMonthlyPayment.setUser(user);

        Payment expiredYearlyPayment = new Payment();
        expiredYearlyPayment.setAt(now.minusYears(2));
        expiredYearlyPayment.setRecurringInterval(Payment.YEAR);
        expiredYearlyPayment.setUser(user);

        when(notchPayProvider.payments()).thenReturn(List.of(almostExpiredMonthlyPayment, expiredYearlyPayment));

        //when
        paymentReminder.sendStats();

        //then
        verify(notchPayProvider, never()).createSession(any(), any(), any());
        verify(userProvider, never()).save(any());
        verify(emailSender, never()).send(any(), anyBoolean());


    }


}