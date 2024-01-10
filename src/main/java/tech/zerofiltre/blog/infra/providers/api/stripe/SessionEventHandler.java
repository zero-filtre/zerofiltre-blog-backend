package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionListLineItemsParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.HashMap;
import java.util.Map;

import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.*;

@Slf4j
@Component
public class SessionEventHandler {

    private final StripeCommons stripeCommons;
    private final InfraProperties infraProperties;

    public SessionEventHandler(StripeCommons stripeCommons, InfraProperties infraProperties) {
        this.stripeCommons = stripeCommons;
        this.infraProperties = infraProperties;
    }

    public void handleSessionCompleted(Event event, Session stripeObject) throws StripeException, ZerofiltreException { //one shot payment
        String userId;
        Customer customer;
        SessionRetrieveParams params = SessionRetrieveParams.builder()
                .addExpand("line_items")
                .addExpand("customer")
                .addExpand("payment_intent")
                .build();

        Session session = Session.retrieve(stripeObject.getId(), params, null);

        SessionListLineItemsParams listLineItemsParams = SessionListLineItemsParams.builder()
                .addExpand("data.price.product")
                .build();

        LineItemCollection lineItems = session.listLineItems(listLineItemsParams);
        customer = session.getCustomerObject();
        log.info("EventId= {}, EventType={}, Customer: {}", event.getId(), event.getType(), customer != null ? customer.toString().replace("\n", " ") : "no customer provided");
        userId = customer != null && customer.getMetadata() != null ? customer.getMetadata().get(USER_ID) : "";

        //lineItems contains only one lineItem
        LineItem lineItem = lineItems.getData().get(0);

        log.info("EventId= {}, EventType={}, Handling Line item: {}", event.getId(), event.getType(), lineItem.toString().replace("\n", " "));

        Price price = lineItem.getPrice();
        log.info(EVENT_ID_EVENT_TYPE_PRICE, event.getId(), event.getType(), price.toString().replace("\n", " "));

        com.stripe.model.Product productObject = price.getProductObject();

        stripeCommons.fulfillOrder(userId, productObject, true, event, customer);
        Map<String, String> metadata = productObject != null ? productObject.getMetadata() : new HashMap<>();
        notifyUser(event, customer, metadata);

    }

    private void notifyUser(Event event, Customer customer, Map<String, String> metadata) {
        if (customer != null) {
            String originURL = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
            long productId;
            try {
                productId = Long.parseLong(metadata.get(PRODUCT_ID));
                stripeCommons.notifyUser(customer,
                        VOTRE_PAIEMENT_CHEZ_ZEROFILTRE,
                        "Merci de faire confiance à Zerofiltre.tech, vous pouvez dès à présent débuter votre apprentissage: \n\n"
                                + originURL + "/cours/" + productId
                                + "\n\n");
            } catch (NumberFormatException e) {
                log.info("EventId = {}, EventType = {},This is a pro subscription on product {}, the notification be handled by invoice.paid handler", event.getId(), event.getType(), PRODUCT_ID);
            }
        }
    }
}
