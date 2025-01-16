package tech.zerofiltre.blog.infra.providers.notification.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.infra.providers.api.notchpay.NotchPayProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @Test
    void reminderIgnores_expiredPayments() throws PaymentException {
        //Given
        Payment payment = new Payment();
        payment.setExpired(true);

        //when
        paymentReminder.notify(payment, 4);

        //then
        verify(notchPayProvider, times(0)).createSession(any(), any(), any());
    }

    @Test
    void finalReminder_expiresPayment() throws PaymentException {

        //Given
        Payment payment = new Payment();

        //when
        paymentReminder.notify(payment, 0);

        //then
        Assertions.assertThat(payment.isExpired()).isTrue();

    }


}