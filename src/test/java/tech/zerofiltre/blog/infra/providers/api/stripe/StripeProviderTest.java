package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.google.gson.*;
import com.stripe.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeProviderTest {

    private static final String OUR_DATA_CONTENT = "{\n" +
            "      \"id\":\"cs_test_a1cYkLXTWXgpbjAZX8qIZL5Bl5Vbr1JrICWu75V1BlpzXo2tSbiWyQayKu\",\n" +
            "      \"object\":\"checkout.session\",\n" +
            "      \"after_expiration\":null,\n" +
            "      \"allow_promotion_codes\":null,\n" +
            "      \"amount_subtotal\":999,\n" +
            "      \"amount_total\":999,\n" +
            "      \"automatic_tax\":{\n" +
            "         \"enabled\":true,\n" +
            "         \"status\":\"complete\"\n" +
            "      },\n" +
            "      \"billing_address_collection\":null,\n" +
            "      \"cancel_url\":\"https://dev.zerofiltre.tech/payment/cancel\",\n" +
            "      \"client_reference_id\":null,\n" +
            "      \"consent\":null,\n" +
            "      \"consent_collection\":null,\n" +
            "      \"created\":1685272983,\n" +
            "      \"currency\":\"eur\",\n" +
            "      \"currency_conversion\":null,\n" +
            "      \"custom_fields\":[\n" +
            "         \n" +
            "      ],\n" +
            "      \"custom_text\":{\n" +
            "         \"shipping_address\":null,\n" +
            "         \"submit\":null\n" +
            "      },\n" +
            "      \"customer\":\"cus_NhZalBCm03WArO\",\n" +
            "      \"customer_creation\":null,\n" +
            "      \"customer_details\":{\n" +
            "         \"address\":{\n" +
            "            \"city\":null,\n" +
            "            \"country\":\"FR\",\n" +
            "            \"line1\":null,\n" +
            "            \"line2\":null,\n" +
            "            \"postal_code\":null,\n" +
            "            \"state\":null\n" +
            "         },\n" +
            "         \"email\":\"obele@gmail.com\",\n" +
            "         \"name\":\"Obele obele\",\n" +
            "         \"phone\":null,\n" +
            "         \"tax_exempt\":\"none\",\n" +
            "         \"tax_ids\":[\n" +
            "            \n" +
            "         ]\n" +
            "      },\n" +
            "      \"customer_email\":null,\n" +
            "      \"expires_at\":1685359383,\n" +
            "      \"invoice\":\"in_1NChgpFbuS9bqsyPGceMWalv\",\n" +
            "      \"invoice_creation\":null,\n" +
            "      \"livemode\":false,\n" +
            "      \"locale\":\"fr\",\n" +
            "      \"metadata\":{\n" +
            "         \n" +
            "      },\n" +
            "      \"mode\":\"subscription\",\n" +
            "      \"payment_intent\":null,\n" +
            "      \"payment_link\":null,\n" +
            "      \"payment_method_collection\":\"always\",\n" +
            "      \"payment_method_options\":null,\n" +
            "      \"payment_method_types\":[\n" +
            "         \"card\"\n" +
            "      ],\n" +
            "      \"payment_status\":\"paid\",\n" +
            "      \"phone_number_collection\":{\n" +
            "         \"enabled\":false\n" +
            "      },\n" +
            "      \"recovered_from\":null,\n" +
            "      \"setup_intent\":null,\n" +
            "      \"shipping_address_collection\":null,\n" +
            "      \"shipping_cost\":null,\n" +
            "      \"shipping_details\":null,\n" +
            "      \"shipping_options\":[\n" +
            "         \n" +
            "      ],\n" +
            "      \"status\":\"complete\",\n" +
            "      \"submit_type\":null,\n" +
            "      \"subscription\":\"sub_1NChgpFbuS9bqsyPZHPXNIhh\",\n" +
            "      \"success_url\":\"https://dev.zerofiltre.tech/payment/success\",\n" +
            "      \"total_details\":{\n" +
            "         \"amount_discount\":0,\n" +
            "         \"amount_shipping\":0,\n" +
            "         \"amount_tax\":167\n" +
            "      },\n" +
            "      \"url\":null\n" +
            "   }";

    private static final String NOT_OUR_DATA_CONTENT = "{\n" +
            "         \"id\":\"cs_live_a133JZC3hoE821ZfEzQrz7iywAxi4yRWbOzIVwgSJTkeLCBGmZpL7RBOZ2\",\n" +
            "         \"object\":\"checkout.session\",\n" +
            "         \"after_expiration\":null,\n" +
            "         \"allow_promotion_codes\":false,\n" +
            "         \"amount_subtotal\":2000,\n" +
            "         \"amount_total\":2000,\n" +
            "         \"automatic_tax\":{\n" +
            "            \"enabled\":true,\n" +
            "            \"status\":\"complete\"\n" +
            "         },\n" +
            "         \"billing_address_collection\":\"auto\",\n" +
            "         \"cancel_url\":\"https://stripe.com\",\n" +
            "         \"client_reference_id\":null,\n" +
            "         \"consent\":null,\n" +
            "         \"consent_collection\":{\n" +
            "            \"promotions\":\"none\",\n" +
            "            \"terms_of_service\":\"none\"\n" +
            "         },\n" +
            "         \"created\":1685017834,\n" +
            "         \"currency\":\"xaf\",\n" +
            "         \"currency_conversion\":null,\n" +
            "         \"custom_fields\":[\n" +
            "            \n" +
            "         ],\n" +
            "         \"custom_text\":{\n" +
            "            \"shipping_address\":null,\n" +
            "            \"submit\":null\n" +
            "         },\n" +
            "         \"customer\":null,\n" +
            "         \"customer_creation\":\"if_required\",\n" +
            "         \"customer_details\":{\n" +
            "            \"address\":{\n" +
            "               \"city\":null,\n" +
            "               \"country\":\"CD\",\n" +
            "               \"line1\":null,\n" +
            "               \"line2\":null,\n" +
            "               \"postal_code\":null,\n" +
            "               \"state\":null\n" +
            "            },\n" +
            "            \"email\":\"benoitkavothah13@gmail.com\",\n" +
            "            \"name\":\"TIOPP nkk\",\n" +
            "            \"phone\":\"+243972906285\",\n" +
            "            \"tax_exempt\":\"none\",\n" +
            "            \"tax_ids\":[\n" +
            "               \n" +
            "            ]\n" +
            "         },\n" +
            "         \"customer_email\":null,\n" +
            "         \"expires_at\":1685104233,\n" +
            "         \"invoice\":null,\n" +
            "         \"invoice_creation\":{\n" +
            "            \"enabled\":false,\n" +
            "            \"invoice_data\":{\n" +
            "               \"account_tax_ids\":null,\n" +
            "               \"custom_fields\":null,\n" +
            "               \"description\":null,\n" +
            "               \"footer\":null,\n" +
            "               \"metadata\":{\n" +
            "                  \n" +
            "               },\n" +
            "               \"rendering_options\":null\n" +
            "            }\n" +
            "         },\n" +
            "         \"livemode\":true,\n" +
            "         \"locale\":\"auto\",\n" +
            "         \"metadata\":{\n" +
            "            \n" +
            "         },\n" +
            "         \"mode\":\"payment\",\n" +
            "         \"payment_intent\":\"pi_3NBdLUFbuS9bqsyP1uWpZjI2\",\n" +
            "         \"payment_link\":\"plink_1MkyAvFbuS9bqsyPUb5ckZA1\",\n" +
            "         \"payment_method_collection\":\"always\",\n" +
            "         \"payment_method_options\":{\n" +
            "            \n" +
            "         },\n" +
            "         \"payment_method_types\":[\n" +
            "            \"card\"\n" +
            "         ],\n" +
            "         \"payment_status\":\"paid\",\n" +
            "         \"phone_number_collection\":{\n" +
            "            \"enabled\":true\n" +
            "         },\n" +
            "         \"recovered_from\":null,\n" +
            "         \"setup_intent\":null,\n" +
            "         \"shipping_address_collection\":null,\n" +
            "         \"shipping_cost\":null,\n" +
            "         \"shipping_details\":null,\n" +
            "         \"shipping_options\":[\n" +
            "            \n" +
            "         ],\n" +
            "         \"status\":\"complete\",\n" +
            "         \"submit_type\":\"auto\",\n" +
            "         \"subscription\":null,\n" +
            "         \"success_url\":\"https://stripe.com\",\n" +
            "         \"total_details\":{\n" +
            "            \"amount_discount\":0,\n" +
            "            \"amount_shipping\":0,\n" +
            "            \"amount_tax\":333\n" +
            "         },\n" +
            "         \"url\":null\n" +
            "      }";

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


    StripeProvider stripeProvider;

    @BeforeEach
    void init() {
        stripeProvider = new StripeProvider(infraProperties, sessionEventHandler, invoiceEventHandler, userProvider, metricsProvider, userNotificationProvider);
        lenient().doNothing().when(userNotificationProvider).notify(any());
    }


    @Test
    void returnTrue_ifEventNotForUs() {
        //given
        Event.Data data = new Event.Data();

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(NOT_OUR_DATA_CONTENT, JsonObject.class);
        data.setObject(jsonObject);

        when(event.getData()).thenReturn(data);
        when(event.getType()).thenReturn("checkout.session.completed");
        when(infraProperties.getEnv()).thenReturn("prod");

        //when
        boolean result = stripeProvider.isEventNotForUs(event);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void returnFalse_ifEventForUs() {
        //given
        Event.Data data = new Event.Data();

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(OUR_DATA_CONTENT, JsonObject.class);
        data.setObject(jsonObject);

        when(event.getData()).thenReturn(data);
        when(event.getType()).thenReturn("checkout.session.completed");
        when(infraProperties.getEnv()).thenReturn("dev");

        //when
        boolean result = stripeProvider.isEventNotForUs(event);

        //then
        assertThat(result).isFalse();
    }
}