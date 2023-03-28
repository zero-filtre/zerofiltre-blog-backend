package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.*;
import com.stripe.model.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.infra.*;

import java.util.*;

import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.*;

@Slf4j
@Component
public class InvoiceEventHandler {

    private final StripeCommons stripeCommons;
    private final InfraProperties infraProperties;

    public InvoiceEventHandler(StripeCommons stripeCommons, InfraProperties infraProperties) {
        this.stripeCommons = stripeCommons;
        this.infraProperties = infraProperties;
    }

    public void handleInvoicePaid(Event event, Customer customer, String userId, Invoice invoice, InvoiceLineItemCollection items, boolean isProPlan, Subscription subscription) throws StripeException, BlogException {
        if (SUBSCRIPTION_CREATE_BILLING_REASON.equals(invoice.getBillingReason())) {
            notifyUser(event, customer, userId, invoice, subscription, 1);
            return;
        }

        InvoiceLineItem lineItem = items.getData().get(0);
        log.info("EventId= {}, EventType={}, Handling Line item: {}", event.getId(), event.getType(), lineItem.toString().replace("\n", " "));

        Price price = lineItem.getPrice();
        log.info(EVENT_ID_EVENT_TYPE_PRICE, event.getId(), event.getType(), price.toString().replace("\n", " "));

        com.stripe.model.Product productObject = price.getProductObject();
        if (PRO_PLAN_PRODUCT_ID.equals(productObject.getId())) { //subscription to PRO plan
            isProPlan = true;
        }
        stripeCommons.fulfillOrder(userId, productObject, true, event, customer);

        int totalPaidCount = Integer.parseInt(subscription.getMetadata().get(TOTAL_PAID_COUNT));
        int count = totalPaidCount + 1;
        notifyUser(event, customer, userId, invoice, subscription, count);

        if (!isProPlan && count >= 3) {
            subscription.cancel();
            log.info("EventId= {}, EventType={}, User {} final invoice " + count + " paid and subscription cancelled {}", event.getId(), event.getType(), userId, subscription.getId());
        }
    }


    public void handleInvoicePaymentFailed(Event event, Customer customer, String userId, Invoice invoice, InvoiceLineItemCollection items, Subscription subscription) throws BlogException {
        String customerPortalLink = infraProperties.getCustomerPortalLink();
        if (SUBSCRIPTION_CREATE_BILLING_REASON.equals(invoice.getBillingReason())) {
            notifyFailure(event, customer, userId, subscription, customerPortalLink);
            return;
        }
        InvoiceLineItem lineItem = items.getData().get(0);
        log.info("EventId= {}, EventType={},Line item: {}", event.getId(), event.getType(), lineItem.toString().replace("\n", " "));

        Price price = lineItem.getPrice();
        log.info(EVENT_ID_EVENT_TYPE_PRICE, event.getId(), event.getType(), price.toString().replace("\n", " "));

        com.stripe.model.Product productObject = price.getProductObject();
        stripeCommons.fulfillOrder(userId, productObject, false, event, customer);
        notifyFailure(event, customer, userId, subscription, customerPortalLink);
    }

    private void notifyFailure(Event event, Customer customer, String userId, Subscription subscription, String customerPortalLink) {
        if (customer != null) {
            stripeCommons.notifyUser(customer,
                    "[Urgent] Paiement échoué",
                    "Le paiement de votre facture a échoué, votre abonnement ne sera pas (re)activé. "
                            + "\n Vous pouvez vous suivre le lien ci-dessous pour mettre à jour vos moyens de paiement et essayer de nouveau."
                            + " \n Servez-vous de l'adresse e-mail utilisée lors du paiement."
                            + "\n Vous pouvez copier et coller le lien dans votre navigateur internet si jamais cliquer dessus ne fonctionne pas."
                            + "\n" + customerPortalLink
                            + SIGNATURE);
        }
        log.info("EventId= {}, EventType={}, Invoice payment failed for User {} on subscription {}", event.getId(), event.getType(), userId, subscription.getId());
    }


    private void notifyUser(Event event, Customer customer, String userId, Invoice invoice, Subscription subscription, int billCount) throws StripeException {
        subscription.getMetadata().put(TOTAL_PAID_COUNT, String.valueOf(billCount));
        subscription.update(Map.of("metadata", subscription.getMetadata()));
        if (customer != null)
            stripeCommons.notifyUser(customer,
                    VOTRE_PAIEMENT_CHEZ_ZEROFILTRE,
                    "Merci de faire confiance à Zerofiltre.tech, retrouvez votre facture ci-dessous: "
                            + "\n" + invoice.getInvoicePdf()
                            + SIGNATURE);

        log.info("EventId= {}, EventType={}, User {} Invoice #{} paid for subscription {}: {}", event.getId(), event.getType(), userId, billCount, billCount > 1 ? "renewal " : "creation ", subscription.getId());
    }


}
