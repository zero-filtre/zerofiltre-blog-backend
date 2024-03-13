package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.metrics.model.CounterSpecs;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.PaymentProvider;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Locale;

@Slf4j
@Component
public class StripeProvider implements PaymentProvider {

    private static final String USER_ID = "userId";
    public static final String PRODUCT_ID = "productId";
    public static final String INVALID_PAYLOAD = "Invalid payload";
    public static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
    public static final String CUSTOMER_SUBSCRIPTION_DELETED = "customer.subscription.deleted";


    private final InfraProperties infraProperties;
    private final SessionEventHandler sessionEventHandler;

    private final SubscriptionEventHandler subscriptionEventHandler;
    private final InvoiceEventHandler invoiceEventHandler;
    private final UserProvider userProvider;
    private final MetricsProvider metricsProvider;
    private final UserNotificationProvider userNotificationProvider;

    private final EventChecker eventChecker;

    public StripeProvider(InfraProperties infraProperties, SessionEventHandler sessionEventHandler, SubscriptionEventHandler subscriptionEventHandler, InvoiceEventHandler invoiceEventHandler, UserProvider userProvider, MetricsProvider metricsProvider, UserNotificationProvider userNotificationProvider, EventChecker eventChecker) {
        this.infraProperties = infraProperties;

        this.sessionEventHandler = sessionEventHandler;
        this.subscriptionEventHandler = subscriptionEventHandler;
        this.invoiceEventHandler = invoiceEventHandler;
        this.userProvider = userProvider;
        this.metricsProvider = metricsProvider;
        this.userNotificationProvider = userNotificationProvider;
        this.eventChecker = eventChecker;
    }


    @Override
    public String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException {
        CounterSpecs counterSpecs = new CounterSpecs();
        counterSpecs.setName(CounterSpecs.ZEROFILTRE_CHECKOUT_CREATIONS);
        String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());


        try {

            CustomerSearchParams customerSearchParams = CustomerSearchParams.builder()
                    .setQuery("metadata['" + USER_ID + "']:'" + user.getId() + "'")
                    .build();

            CustomerSearchResult result = Customer.search(customerSearchParams);
            String session;
            if (!result.getData().isEmpty()) {
                Customer customer = result.getData().get(0);
                log.info("Customer for user {} found on stripe, using him to create checkout session.: {}", user.getEmail(), customer.toString().replace("\n", " "));
                session = createSession(chargeRequest, product, customer);
                userNotificationProvider.notify(new UserActionEvent(appUrl, Locale.forLanguageTag(user.getLanguage()), user, null, null, Action.CHECKOUT_STARTED));
                counterSpecs.setTags("foundCustomer", "true", "success", "true");
                return session;

            }
            session = createSession(chargeRequest, product, createCustomer(user));
            counterSpecs.setTags("foundCustomer", "false", "success", "true");
            userNotificationProvider.notify(new UserActionEvent(appUrl, Locale.forLanguageTag(user.getLanguage()), user, null, null, Action.CHECKOUT_STARTED));
            metricsProvider.incrementCounter(counterSpecs);
            return session;
        } catch (StripeException e) {
            log.error("Error while initializing the checkout session: " + e.getLocalizedMessage(), e);
            throw new PaymentException("Error while initializing the checkout session" + e.getLocalizedMessage(), "");
        }
    }

    @Override
    public String handleWebhook(String payload, String sigHeader) throws PaymentException {

        try {
            Event event = eventChecker.checkAndProvideEvent(payload, sigHeader);
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject()
                    .orElseThrow(() -> new IllegalArgumentException(INVALID_PAYLOAD));

            Customer customer;
            String userId;
            InvoiceRetrieveParams invoiceRetrieveParams;

            if (CHECKOUT_SESSION_COMPLETED.equals(event.getType())) {
                sessionEventHandler.handleSessionCompleted(event, (Session) stripeObject);
            } else if (CUSTOMER_SUBSCRIPTION_DELETED.equals(event.getType())) {
                subscriptionEventHandler.handleSubscriptionDeleted(event, (Subscription) stripeObject);
            } else {
                invoiceRetrieveParams = InvoiceRetrieveParams.builder()
                        .addExpand("customer")
                        .addExpand("subscription")
                        .build();
                Invoice invoice = Invoice.retrieve(((Invoice) stripeObject).getId(), invoiceRetrieveParams, null);
                log.info("EventId= {}, EventType={}, Invoice {}", event.getId(), event.getType(), invoice.toString().replace("\n", " "));

                customer = invoice.getCustomerObject();
                log.info("EventId= {}, EventType={}, Customer: {}", event.getId(), event.getType(), customer != null ? customer.toString().replace("\n", " ") : "no customer provided");
                userId = customer != null && customer.getMetadata() != null ? customer.getMetadata().get(USER_ID) : "";
                log.info("EventId= {}, EventType={}, User id: {}", event.getId(), event.getType(), userId);

                InvoiceLineItemCollectionListParams itemListParams = InvoiceLineItemCollectionListParams.builder()
                        .addExpand("data.price.product")
                        .build();
                InvoiceLineItemCollection items = invoice.getLines().list(itemListParams);
                boolean isProPlan = false;

                Subscription subscription = invoice.getSubscriptionObject();
                log.info("EventId= {}, EventType={}, Subscription: {}", event.getId(), event.getType(), subscription.toString().replace("\n", " "));

                if ("invoice.paid".equals(event.getType())) {

                    invoiceEventHandler.handleInvoicePaid(event, customer, userId, invoice, items, isProPlan, subscription);
                } else if ("invoice.payment_failed".equals(event.getType())) {
                    invoiceEventHandler.handleInvoicePaymentFailed(event, customer, userId, invoice, items, subscription);
                } else {
                    log.info("Unhandled event type: " + event.getType());
                }
            }
            return "OK";
        } catch (Exception e) {
            throw new PaymentException("An error occurred during payment fulfillment", e, "");
        }
    }

    @Override
    public void cancelSubscription(String paymentCustomerId) throws PaymentException {
        try {
            cancelForPrice(paymentCustomerId, infraProperties.getProPlanPriceId());
            cancelForPrice(paymentCustomerId, infraProperties.getProPlanYearlyPriceId());
        } catch (StripeException e) {
            throw new PaymentException("Error while cancelling the subscription", e, "");
        }
    }

    private static void cancelForPrice(String paymentCustomerId, String priceId) throws StripeException {
        if (paymentCustomerId == null || paymentCustomerId.isBlank()) {
            log.info("No customer id provided, skipping");
            return;
        }
        SubscriptionListParams subscriptionListParams = SubscriptionListParams.builder()
                .setCustomer(paymentCustomerId)
                .setPlan(priceId)
                .build();

        SubscriptionCollection subscriptions = Subscription.list(subscriptionListParams);

        if (subscriptions.getData().isEmpty()) {
            log.info("No subscription found for customer id: {}", paymentCustomerId);
            return;
        }
        for (Subscription subscription : subscriptions.getData()) {
            if (subscription.getStatus().equals("active")) {
                log.info("Subscription id  {} found for customer id: {}, cancelling it", subscription.getId(), paymentCustomerId);
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();
                subscription.update(params);
            }
        }
    }


    private Customer createCustomer(User user) throws StripeException {

        CustomerCreateParams.Builder customerParamsBuilder = CustomerCreateParams.builder()
                .setName(user.getFullName());

        if (EmailValidator.validateEmail(user.getEmail()))
            customerParamsBuilder.setEmail(user.getEmail());

        customerParamsBuilder.putMetadata(USER_ID, String.valueOf(user.getId()));
        Customer customer = Customer.create(customerParamsBuilder.build());
        log.info("Customer created on stripe, he will be used to create the session: {}", customer.toString().replace("\n", " "));
        user.setPaymentCustomerId(customer.getId());
        userProvider.save(user);
        return customer;
    }

    private static void setMonthlyRecurring(SessionCreateParams.LineItem.PriceData.Builder priceDataBuilder) {
        SessionCreateParams.LineItem.PriceData.Recurring recurring = SessionCreateParams.LineItem.PriceData.Recurring.builder()
                .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                .build();
        priceDataBuilder.setRecurring(recurring);
    }

    private String createSession(ChargeRequest chargeRequestVM, Product product, Customer customer) throws StripeException {

        SessionCreateParams.Mode mode = SessionCreateParams.Mode.valueOf(chargeRequestVM.getMode().toUpperCase());

        SessionCreateParams.LineItem.Builder lineItemBuilder = SessionCreateParams.LineItem.builder()
                .setQuantity(1L);

        SessionCreateParams.LineItem.PriceData.ProductData productData = getProductData(product);

        SessionCreateParams.LineItem.PriceData.Builder priceDataBuilder = getPriceDataBuilder(productData);

        String productPrice = getProductPrice(product, chargeRequestVM, mode);

        if (mode.equals(SessionCreateParams.Mode.SUBSCRIPTION) && chargeRequestVM.isProPlan()) {
            if (ZerofiltreUtils.isMentored(product)) {
                priceDataBuilder.setUnitAmount(Long.parseLong(productPrice));
                setMonthlyRecurring(priceDataBuilder);
                lineItemBuilder.setPriceData(priceDataBuilder.build());
            } else {
                lineItemBuilder.setPrice(productPrice);
            }
        } else {
            if (mode.equals(SessionCreateParams.Mode.SUBSCRIPTION)) {
                setMonthlyRecurring(priceDataBuilder);
            }
            priceDataBuilder.setUnitAmount(Long.parseLong(productPrice));
            lineItemBuilder.setPriceData(priceDataBuilder.build());
        }


        SessionCreateParams.LineItem lineItem = lineItemBuilder.build();

        SessionCreateParams.AutomaticTax automaticTax = SessionCreateParams.AutomaticTax.builder()
                .setEnabled(true)
                .build();

        SessionCreateParams.CustomerUpdate customerUpdate = SessionCreateParams.CustomerUpdate.builder()
                .setAddress(SessionCreateParams.CustomerUpdate.Address.AUTO)
                .build();

        String originURL = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(mode)
                .setSuccessUrl(originURL + "/payment/success")
                .setCancelUrl(originURL + "/payment/cancel")
                .setCustomer(customer.getId())
                .setCustomerUpdate(customerUpdate)
                .setLocale(SessionCreateParams.Locale.FR)
                .setAutomaticTax(automaticTax)
                .addLineItem(lineItem);


        SessionCreateParams sessionCreateParams = sessionBuilder.build();
        Session session = Session.create(sessionCreateParams);
        return session.getUrl();

    }

    String getProductPrice(Product product, ChargeRequest chargeRequestVM, SessionCreateParams.Mode mode) {
        if (mode.equals(SessionCreateParams.Mode.SUBSCRIPTION) && chargeRequestVM.isProPlan()) {
            if (ZerofiltreUtils.isMentored(product)) {
                return String.valueOf(product.getPrice());
            } else {
                if ("month".equals(chargeRequestVM.getRecurringInterval())) {
                    return infraProperties.getProPlanPriceId();
                }
                return infraProperties.getProPlanYearlyPriceId();
            }
        } else {
            long productPrice = product.getPrice();
            if (mode.equals(SessionCreateParams.Mode.SUBSCRIPTION)) {
                productPrice = getProductMonthlyPrice(product);
            }
            return String.valueOf(productPrice);
        }
    }

    private static SessionCreateParams.LineItem.PriceData.Builder getPriceDataBuilder(SessionCreateParams.LineItem.PriceData.ProductData productData) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(ChargeRequest.Currency.EUR.getValue())
                .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.INCLUSIVE)
                .setProductData(productData);
    }

    private static SessionCreateParams.LineItem.PriceData.ProductData getProductData(Product product) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .putMetadata(PRODUCT_ID, String.valueOf(product.getId()))
                .setName(product.getTitle())
                .setDescription(product.getSummary())
                .addImage(product.getThumbnail() == null || product.getThumbnail().isBlank() ?
                        "https://ik.imagekit.io/lfegvix1p/tr:w-800,ar-auto,dpr-auto,di-Article_default_oz_Yb-VZj.svg/not_found_image.jpg"
                        : product.getThumbnail())
                .build();
    }

    private long getProductMonthlyPrice(Product product) {
        if (ZerofiltreUtils.isMentored(product))
            return product.getPrice();
        return product.getPrice() / 3 + 1;
    }


}
