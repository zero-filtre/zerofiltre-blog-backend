package tech.zerofiltre.blog.infra.entrypoints.rest.payment;

import com.stripe.*;
import com.stripe.exception.*;
import com.stripe.model.*;
import com.stripe.model.checkout.*;
import com.stripe.net.*;
import com.stripe.param.*;
import com.stripe.param.checkout.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.use_cases.subscription.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.payment.*;
import tech.zerofiltre.blog.domain.payment.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.payment.model.*;
import tech.zerofiltre.blog.infra.security.config.*;
import tech.zerofiltre.blog.util.*;

import javax.validation.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    public static final String INVALID_PAYLOAD = "Invalid payload";
    public static final String PRODUCT_ID = "productId";
    private static final String USER_ID = "userId";
    public static final String SUBSCRIPTION_CREATE_BILLING_REASON = "subscription_create";
    public static final String PRO_PLAN_PRICE_ID = "price_1MjU8aFbuS9bqsyPr9G6P45y";
    public static final String PRO_PLAN_YEARLY_PRICE_ID = "price_1MnyeSFbuS9bqsyPZVGSeHgA";
    public static final String TOTAL_PAID_COUNT = "totalPaidCount";
    public static final String PRO_PLAN_PRODUCT_ID = "prod_NUT4DYfDGPiLbR";
    private final SecurityContextManager securityContextManager;
    private final CourseProvider courseProvider;
    private final InfraProperties infraProperties;
    private final Subscribe subscribe;
    private final Suspend suspend;
    private final UserProvider userProvider;


    public PaymentController(SecurityContextManager securityContextManager, CourseProvider courseProvider, InfraProperties infraProperties, ChapterProvider chapterProvider, UserProvider userProvider, SubscriptionProvider subscriptionProvider) {
        this.securityContextManager = securityContextManager;
        this.courseProvider = courseProvider;
        this.infraProperties = infraProperties;
        this.userProvider = userProvider;
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);
        suspend = new Suspend(subscriptionProvider, courseProvider, chapterProvider);
        Stripe.apiKey = infraProperties.getStripeSecretKey();
    }

    @GetMapping("/success")
    public String success() {
        return "Payment successful";
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "Payment canceled";
    }

    @PostMapping("/checkout")
    public String createCheckoutSession(@RequestBody @Valid ChargeRequestVM chargeRequestVM) throws PaymentException, ResourceNotFoundException {
        User user = securityContextManager.getAuthenticatedUser();
        Product product = getProduct(chargeRequestVM.getProductId(), chargeRequestVM.getProductType());

        try {

            CustomerSearchParams customerSearchParams = CustomerSearchParams.builder()
                    .setQuery("metadata['" + USER_ID + "']:'" + user.getId() + "'")
                    .build();

            CustomerSearchResult result = Customer.search(customerSearchParams);
            if (!result.getData().isEmpty()) {
                Customer customer = result.getData().get(0);
                log.info("Customer for user {} found on stripe, using him to create checkout session.: {}", user.getEmail(), customer.toString().replace("\n", " "));
                return createSession(chargeRequestVM, product, customer);
            }
            return createSession(chargeRequestVM, product, createCustomer(user));
        } catch (StripeException e) {
            log.error("Error while initializing the checkout session: " + e.getLocalizedMessage(), e);
            throw new PaymentException("Error while initializing the checkout session" + e.getLocalizedMessage(), "");
        }

    }


    @PostMapping("/webhook")
    public String handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException, BlogException {

        log.debug("Payload: {}", payload.replace("\n", " "));
        log.info("Signature: {}", sigHeader);

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, infraProperties.getStripeWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid signature", e);
        } catch (Exception e) {
            throw new IllegalArgumentException(INVALID_PAYLOAD, e);
        }

        log.info("Event: {}", event.getType());

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new IllegalArgumentException(INVALID_PAYLOAD));

        Customer customer;
        String userId;
        InvoiceRetrieveParams invoiceRetrieveParams;

        if ("checkout.session.completed".equals(event.getType())) {
            SessionRetrieveParams params = SessionRetrieveParams.builder()
                    .addExpand("line_items")
                    .addExpand("customer")
                    .addExpand("payment_intent")
                    .build();

            Session session = Session.retrieve(((Session) stripeObject).getId(), params, null);

            SessionListLineItemsParams listLineItemsParams = SessionListLineItemsParams.builder()
                    .addExpand("data.price.product")
                    .build();

            LineItemCollection lineItems = session.listLineItems(listLineItemsParams);
            customer = session.getCustomerObject();
            log.info("Customer: {}", customer != null ? customer.toString().replace("\n", " ") : "no customer provided");
            userId = customer != null && customer.getMetadata() != null ? customer.getMetadata().get(USER_ID) : "";

            for (LineItem lineItem : lineItems.getData()) {
                log.info("Line item: {}", lineItem.toString().replace("\n", " "));

                Price price = lineItem.getPrice();
                log.info("Price: {}", price.toString().replace("\n", " "));

                com.stripe.model.Product productObject = price.getProductObject();

                act(userId, productObject, true);
            }

        } else {
            invoiceRetrieveParams = InvoiceRetrieveParams.builder()
                    .addExpand("customer")
                    .addExpand("subscription")
                    .build();
            Invoice invoice = Invoice.retrieve(((Invoice) stripeObject).getId(), invoiceRetrieveParams, null);
            log.info("Invoice {}", invoice.toString().replace("\n", " "));

            customer = invoice.getCustomerObject();
            log.info("Customer: {}", customer != null ? customer.toString().replace("\n", " ") : "no customer provided");
            userId = customer != null && customer.getMetadata() != null ? customer.getMetadata().get(USER_ID) : "";
            log.info("User id: {}", userId);

            InvoiceLineItemCollectionListParams itemListParams = InvoiceLineItemCollectionListParams.builder()
                    .addExpand("data.price.product")
                    .build();
            InvoiceLineItemCollection items = invoice.getLines().list(itemListParams);
            boolean isProPlan = false;

            Subscription subscription = invoice.getSubscriptionObject();
            log.info("Subscription: {}", subscription.toString().replace("\n", " "));

            if ("invoice.paid".equals(event.getType())) {

                if (SUBSCRIPTION_CREATE_BILLING_REASON.equals(invoice.getBillingReason())) {

                    subscription.getMetadata().put(TOTAL_PAID_COUNT, String.valueOf(1));
                    subscription.update(Map.of("metadata", subscription.getMetadata()));
                    //TODO send the bill to the user
                    log.info("User {} Invoice " + 1 + " paid for subscription creation {}", userId, subscription.getId());
                    return "OK";
                }

                for (InvoiceLineItem lineItem : items.getData()) {
                    log.info("Line item: {}", lineItem.toString().replace("\n", " "));

                    Price price = lineItem.getPrice();
                    log.info("Price: {}", price.toString().replace("\n", " "));

                    com.stripe.model.Product productObject = price.getProductObject();
                    if (PRO_PLAN_PRODUCT_ID.equals(productObject.getId())) { //subscription to PRO plan
                        isProPlan = true;
                    }
                    act(userId, productObject, true);

                }

                int totalPaidCount = Integer.parseInt(subscription.getMetadata().get(TOTAL_PAID_COUNT));
                int count = totalPaidCount + 1;
                subscription.getMetadata().put(TOTAL_PAID_COUNT, String.valueOf(count));
                subscription.update(Map.of("metadata", subscription.getMetadata()));
                log.info("User {} Invoice " + count + " paid for subscription renewal {}", userId, subscription.getId());

                if (!isProPlan && count >= 3) {
                    subscription.cancel();
                    log.info("User {} final invoice " + count + " paid and subscription cancelled {}", userId, subscription.getId());
                }

            } else if ("invoice.payment_failed".equals(event.getType())) {

                if (SUBSCRIPTION_CREATE_BILLING_REASON.equals(invoice.getBillingReason())) {
                    //TODO notify user the payment failed and invite him to retry
                    return "OK";
                }
                for (InvoiceLineItem lineItem : items.getData()) {
                    log.info("Line item: {}", lineItem.toString().replace("\n", " "));

                    Price price = lineItem.getPrice();
                    log.info("Price: {}", price.toString().replace("\n", " "));

                    com.stripe.model.Product productObject = price.getProductObject();
                    act(userId, productObject, false);
                    //TODO notify user and send the customer portal to update the payment method
                }
                log.info("Invoice payment failed for User {} on subscription {}", userId, subscription.getId());
            } else {
                log.info("Unhandled event type: " + event.getType());
            }
        }
        return "OK";
    }


    private void act(String userId, com.stripe.model.Product productObject, boolean paymentSuccess) throws BlogException {
        if (productObject != null) {
            log.info("Product object: {}", productObject.toString().replace("\n", " "));

            log.info("User id: {}", userId);

            if (PRO_PLAN_PRODUCT_ID.equals(productObject.getId())) { //subscription to PRO
                User user = userProvider.userOfId(Long.parseLong(userId))
                        .orElseThrow(() -> {
                            log.error("We couldn't find the user {} to edit", userId);
                            return new UserNotFoundException("We couldn't find the user " + userId + " to edit", userId);
                        });
                user.setPlan(paymentSuccess ? User.Plan.PRO : User.Plan.BASIC);
                //TODO UPDATE CUSTOMER PAYMENT_EMAIL and save him
                userProvider.save(user);
            } else {
                long productId = Long.parseLong(productObject.getMetadata().get(PRODUCT_ID));
                log.info("Product id: {}", productId);
                if (paymentSuccess) {
                    subscribe.execute(Long.parseLong(userId), productId);
                    //TODO send the bill to the user
                } else {
                    suspend.execute(Long.parseLong(userId), productId);
                }
                //TODO UPDATE CUSTOMER PAYMENT_EMAIL and save him
            }
        }
    }


    private Product getProduct(long productId, ChargeRequest.ProductType productType) throws ResourceNotFoundException {
        if (productType == ChargeRequest.ProductType.COURSE) {
            return courseProvider
                    .courseOfId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product nof found", String.valueOf(productId), ""));
        } else {
            throw new IllegalArgumentException("Product type not supported");
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
        return customer;
    }

    private String createSession(ChargeRequestVM chargeRequestVM, Product product, Customer customer) throws StripeException {

        SessionCreateParams.Mode mode = SessionCreateParams.Mode.valueOf(chargeRequestVM.getMode().toUpperCase());

        SessionCreateParams.LineItem.Builder lineItemBuilder = SessionCreateParams.LineItem.builder()
                .setQuantity(1L);

        if (mode.equals(SessionCreateParams.Mode.SUBSCRIPTION) && chargeRequestVM.isProPlan()) {
            if ("month".equals(chargeRequestVM.getRecurringInterval())) lineItemBuilder.setPrice(PRO_PLAN_PRICE_ID);
            else lineItemBuilder.setPrice(PRO_PLAN_YEARLY_PRICE_ID);
        } else {

            SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .putMetadata(PRODUCT_ID, String.valueOf(product.getId()))
                    .setName(product.getTitle())
                    .setDescription(product.getSummary())
                    .addImage(product.getThumbnail() == null || product.getThumbnail().isBlank() ?
                            "https://ik.imagekit.io/lfegvix1p/tr:w-800,ar-auto,dpr-auto,di-Article_default_oz_Yb-VZj.svg/not_found_image.jpg"
                            : product.getThumbnail())
                    .build();

            SessionCreateParams.LineItem.PriceData.Builder priceDataBuilder = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(ChargeRequest.Currency.EUR.getValue())
                    .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.INCLUSIVE)
                    .setProductData(productData);

            long price = product.getPrice();
            if (mode.equals(SessionCreateParams.Mode.SUBSCRIPTION)) {
                SessionCreateParams.LineItem.PriceData.Recurring recurring = SessionCreateParams.LineItem.PriceData.Recurring.builder()
                        .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                        .build();

                price = product.getPrice() / 3 + 1;
                priceDataBuilder.setRecurring(recurring);
            } else {
                priceDataBuilder.setUnitAmount(product.getPrice());

            }
            priceDataBuilder.setUnitAmount(price);
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

}


