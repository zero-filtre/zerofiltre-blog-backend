package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceEventHandlerTest {


    InvoiceEventHandler invoiceEventHandler;


//    @Test
//    @Disabled("To be implemented by Ibrahima")
//    void should_cancel_if_mentored() throws StripeException {
//        Subscription subscription = mock(Subscription.class);
//
//
//        //
//
//        //act
//        invoiceEventHandler.cancelFor3TimesPayment(null, null, false, subscription, 3, null);
//
//        //assert
//        verify(subscription, times(0)).cancel();
//
//    }
}