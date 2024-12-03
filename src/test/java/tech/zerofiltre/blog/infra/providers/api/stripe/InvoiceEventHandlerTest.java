package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.util.*;

import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.PRODUCT_ID;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.TOTAL_PAID_COUNT;


@ExtendWith(MockitoExtension.class)
class InvoiceEventHandlerTest {

    @Mock
    private StripeCommons stripeCommons;
    @Mock
    private InfraProperties infraProperties;
    @Mock
    private CourseProvider courseProvider;

    private InvoiceEventHandler invoiceEventHandler;

    private Subscription subscription;
    private Product product;
    private Course course;

    @BeforeEach
    void init() {
        invoiceEventHandler = new InvoiceEventHandler(stripeCommons, infraProperties, courseProvider);
        subscription = mock(Subscription.class);
        product = mock(Product.class);
        course = new Course();
        when(product.getMetadata()).thenReturn(Collections.singletonMap(PRODUCT_ID, "1"));
    }

    @Test
    void shouldNotCancel_After3Payments_ifMentored() throws StripeException, ZerofiltreException {
        //arrange
        course.setMentored(true);
        doReturn(Optional.of(course)).when(courseProvider).courseOfId(1);

        //act
        invoiceEventHandler.cancelFor3TimesPayment(null, null, false, subscription, 3, product);

        //assert
        verify(subscription, times(0)).cancel();
    }

    @Test
    void shouldCancel_After3Payments_ifMentored() throws StripeException, ZerofiltreException {
        //arrange
        doReturn(Optional.of(course)).when(courseProvider).courseOfId(1);


        //act
        invoiceEventHandler.cancelFor3TimesPayment(new Event(), "userId", false, subscription, 3, product);

        //assert
        verify(subscription, times(1)).cancel();

    }

    @Test
    void shouldNotTryToCancel_ifProductDoesNotBelongs_ToThePlatform() throws StripeException, ZerofiltreException {
        //arrange
        //No metadata PRODUCT_ID => product not belonging to the platform
        when(product.getMetadata()).thenReturn(Collections.emptyMap());

        //act
        invoiceEventHandler.cancelFor3TimesPayment(new Event(), "userId", false, subscription, 3, product);

        //assert
        verifyNoInteractions(subscription);

    }

    @Test
    void PaymentFailure_ShouldNotFulfillOrders_forProducts_notBelonging_thePlatform() throws ZerofiltreException {
        //arrange
        Invoice invoice = mock(Invoice.class);
        InvoiceLineItemCollection items = mock(InvoiceLineItemCollection.class);
        InvoiceLineItem item = mock(InvoiceLineItem.class);
        Price price = mock(Price.class);
        List<InvoiceLineItem> itemsData = new ArrayList<>();
        itemsData.add(item);
        when(items.getData()).thenReturn(itemsData);
        when(invoice.getBillingReason()).thenReturn("another_reason");
        when(item.getPrice()).thenReturn(price);
        when(price.getProductObject()).thenReturn(product);

        //No metadata PRODUCT_ID => product not belonging to the platform
        when(product.getMetadata()).thenReturn(Collections.emptyMap());

        //act
        invoiceEventHandler.handleInvoicePaymentFailed(new Event(), new Customer(), "12", invoice, items, new Subscription());

        //assert
        verify(stripeCommons, never()).fulfillOrder(anyString(), any(), anyBoolean(), any(), any());
    }

    @Test
    void paymentSuccess_ShouldNotFulfillOrders_forProducts_notBelonging_thePlatform() throws ZerofiltreException, StripeException {
        //arrange
        Invoice invoice = mock(Invoice.class);
        InvoiceLineItemCollection items = mock(InvoiceLineItemCollection.class);
        InvoiceLineItem item = mock(InvoiceLineItem.class);
        Price price = mock(Price.class);
        List<InvoiceLineItem> itemsData = new ArrayList<>();
        itemsData.add(item);
        when(items.getData()).thenReturn(itemsData);
        when(invoice.getBillingReason()).thenReturn("another_reason");
        when(item.getPrice()).thenReturn(price);
        when(price.getProductObject()).thenReturn(product);
        when(infraProperties.getProPlanProductId()).thenReturn("any");

        //No metadata PRODUCT_ID => product not belonging to the platform
        when(product.getMetadata()).thenReturn(Collections.emptyMap());

        when(subscription.getMetadata()).thenReturn(new HashMap<>(Collections.singletonMap(TOTAL_PAID_COUNT, "1")));

        //act
        invoiceEventHandler.handleInvoicePaid(new Event(), new Customer(), "12", invoice, items, false, subscription);

        //assert
        verify(stripeCommons, never()).fulfillOrder(anyString(), any(), anyBoolean(), any(), any());
    }

}