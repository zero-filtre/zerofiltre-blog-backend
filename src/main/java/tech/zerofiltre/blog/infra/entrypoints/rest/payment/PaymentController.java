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

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    public static final String INVALID_PAYLOAD = "Invalid payload";
    public static final String PRODUCT_ID = "productId";
    private static final String USER_ID = "userId";
    private final SecurityContextManager securityContextManager;
    private final CourseProvider courseProvider;
    private final InfraProperties infraProperties;
    private final Subscribe subscribe;
    private final UserProvider userProvider;


    public PaymentController(SecurityContextManager securityContextManager, CourseProvider courseProvider, InfraProperties infraProperties, ChapterProvider chapterProvider, UserProvider userProvider, SubscriptionProvider subscriptionProvider) {
        this.securityContextManager = securityContextManager;
        this.courseProvider = courseProvider;
        this.infraProperties = infraProperties;
        this.userProvider = userProvider;
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider, chapterProvider);
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

        switch (event.getType()) {
            case "checkout.session.completed":
                // Payment is successful and the subscription is created.
                // You should provision the subscription and save the customer ID to your database.
                Session prematureSession = (Session) event.getDataObjectDeserializer().getObject()
                        .orElseThrow(() -> new IllegalArgumentException(INVALID_PAYLOAD));
                SessionRetrieveParams params = SessionRetrieveParams.builder()
                        .addExpand("line_items")
                        .addExpand("customer")
                        .addExpand("payment_intent")
                        .build();

                Session session = Session.retrieve(prematureSession.getId(), params, null);

                SessionListLineItemsParams listLineItemsParams = SessionListLineItemsParams.builder()
                        .addExpand("data.price.product")
                        .build();

                LineItemCollection lineItems = session.listLineItems(listLineItemsParams);
                Customer customer = session.getCustomerObject();
                log.info("Customer: {}", customer != null ? customer.toString().replace("\n", " ") : "no customer provided");
                String userId = customer != null && customer.getMetadata() != null ? customer.getMetadata().get(USER_ID) : "";

                for (LineItem lineItem : lineItems.getData()) {
                    log.info("Line item: {}", lineItem.toString().replace("\n", " "));

                    Price price = lineItem.getPrice();
                    log.info("Price: {}", price.toString().replace("\n", " "));

                    com.stripe.model.Product productObject = price.getProductObject();

                    if (productObject != null) {
                        log.info("Product object: {}", productObject.toString().replace("\n", " "));

                        long productId = Long.parseLong(productObject.getMetadata().get(PRODUCT_ID));
                        log.info("Product id: {}", productId);

                        log.info("User id: {}", userId);

                        if ("prod_NUT4DYfDGPiLbR".equals(productObject.getId())) { //subscription to PRO
                            User user = userProvider.userOfId(Long.parseLong(userId))
                                    .orElseThrow(() -> {
                                        log.error("We couldn't find the user {} to make him PRO", userId);
                                        return new UserNotFoundException("We couldn't find the user " + userId + " to make him PRO", userId);
                                    });
                            user.setPlan(User.Plan.PRO);
                            userProvider.save(user);
                        } else {
                            subscribe.execute(Long.parseLong(userId), productId);
                        }
                    }
                }

                break;
            case "invoice.paid":
                // Continue to provision the subscription as payments continue to be made.
                // Store the status in your database and check when a user accesses your service.
                // This approach helps you avoid hitting rate limits.
                log.info("Invoice paid");
                break;
            case "invoice.payment_failed":
                // The payment failed or the customer does not have a valid payment method.
                // The subscription becomes past_due. Notify your customer and send them to the
                // customer portal to update their payment information.
                log.info("Invoice payment failed");
                break;
            default:
                log.info("Unhandled event type: " + event.getType());
        }
        return "OK";
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

            lineItemBuilder.setPrice("price_1MjU8aFbuS9bqsyPr9G6P45y");
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


