package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Map;

import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.*;

@Slf4j
@Component
public class InvoiceEventHandler {

    private final StripeCommons stripeCommons;
    private final InfraProperties infraProperties;
    private final CourseProvider courseProvider;

    public InvoiceEventHandler(StripeCommons stripeCommons, InfraProperties infraProperties, CourseProvider courseProvider) {
        this.stripeCommons = stripeCommons;
        this.infraProperties = infraProperties;
        this.courseProvider = courseProvider;
    }

    public void handleInvoicePaid(Event event, Customer customer, String userId, Invoice invoice, InvoiceLineItemCollection items, boolean isProPlan, Subscription subscription) throws StripeException, ZerofiltreException {
        if (SUBSCRIPTION_CREATE_BILLING_REASON.equals(invoice.getBillingReason())) {
            notifyUser(event, customer, userId, invoice, subscription, 1);
            return;
        }

        InvoiceLineItem lineItem = items.getData().get(0);
        log.info("EventId= {}, EventType={}, Handling Line item: {}", event.getId(), event.getType(), lineItem.toString().replace("\n", " "));

        Price price = lineItem.getPrice();
        log.info(EVENT_ID_EVENT_TYPE_PRICE, event.getId(), event.getType(), price.toString().replace("\n", " "));

        com.stripe.model.Product productObject = price.getProductObject();
        if (infraProperties.getProPlanProductId().equals(productObject.getId())) { //subscription to PRO plan
            isProPlan = true;
        }
        stripeCommons.fulfillOrder(userId, productObject, true, event, customer);

        int totalPaidCount = Integer.parseInt(subscription.getMetadata().get(TOTAL_PAID_COUNT));
        int count = totalPaidCount + 1;
        notifyUser(event, customer, userId, invoice, subscription, count);

        cancelFor3TimesPayment(event, userId, isProPlan, subscription, count, productObject);
    }

    public void handleInvoicePaymentFailed(Event event, Customer customer, String userId, Invoice invoice, InvoiceLineItemCollection items, Subscription subscription) throws ZerofiltreException {
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

    void cancelFor3TimesPayment(Event event, String userId, boolean isProPlan, Subscription subscription, int count, Product productObject) throws StripeException, ZerofiltreException {
        if (shouldCancel3TimesPayment(isProPlan, count, productObject)) {
            subscription.getMetadata().put(CANCELLED_3TIMES_PAID, Boolean.toString(true));
            subscription.update(Map.of("metadata", subscription.getMetadata()));
            subscription.cancel();
            log.info("EventId= {}, EventType={}, User {} final invoice " + count + " paid and future payments cancelled {}", event.getId(), event.getType(), userId, subscription.getId());
        }
    }

    private tech.zerofiltre.blog.domain.Product getEnrolledProduct(Product productObject) throws ZerofiltreException {
        long id = Long.parseLong(productObject.getMetadata().get(PRODUCT_ID));

        log.info("Get enrolled product productId={}", id);

        return courseProvider
                .courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product nof found", String.valueOf(id), ""));

    }

    private boolean shouldCancel3TimesPayment(boolean isProPlan, int count, Product productObject) throws ZerofiltreException {
        return !isProPlan && count >= 3 && !ZerofiltreUtils.isMentored(getEnrolledProduct(productObject));
    }

    private void notifyFailure(Event event, Customer customer, String userId, Subscription subscription, String customerPortalLink) {
        if (customer != null) {
            stripeCommons.notifyUser(customer,
                    "[Urgent] Paiement échoué",
                    "Le paiement de votre facture a échoué, votre abonnement ne sera pas (re)activé. "
                            + "\n Vous pouvez utiliser le lien ci-dessous pour mettre à jour vos moyens de paiement et essayer de nouveau."
                            + " \n Servez-vous de l'adresse e-mail utilisée lors du dernier paiement."
                            + "\n Vous pouvez copier et coller le lien dans votre navigateur internet si jamais cliquer dessus ne fonctionne pas."
                            + "\n" + customerPortalLink);
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
                            + "\n\n" + invoice.getInvoicePdf());

        log.info("EventId= {}, EventType={}, User {} Invoice #{} paid for subscription {}: {}", event.getId(), event.getType(), userId, billCount, billCount > 1 ? "renewal " : "creation ", subscription.getId());
    }


}
