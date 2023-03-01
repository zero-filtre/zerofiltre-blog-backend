package tech.zerofiltre.blog.infra.entrypoints.rest.payment;

import com.stripe.*;
import com.stripe.exception.*;
import com.stripe.model.*;
import com.stripe.model.checkout.*;
import com.stripe.net.*;
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


    public PaymentController(SecurityContextManager securityContextManager, CourseProvider courseProvider, InfraProperties infraProperties, ChapterProvider chapterProvider, UserProvider userProvider, SubscriptionProvider subscriptionProvider) {
        this.securityContextManager = securityContextManager;
        this.courseProvider = courseProvider;
        this.infraProperties = infraProperties;
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


        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .putMetadata(PRODUCT_ID, String.valueOf(product.getId()))
                .putMetadata(USER_ID, String.valueOf(user.getId()))
                .setName(product.getTitle())
                .setDescription(product.getSummary())
                .addImage(product.getThumbnail() == null || product.getThumbnail().isBlank() ?
                        "https://ik.imagekit.io/lfegvix1p/tr:w-800,ar-auto,dpr-auto,di-Article_default_oz_Yb-VZj.svg/not_found_image.jpg"
                        : product.getThumbnail())
                .build();

        SessionCreateParams.LineItem.PriceData.Builder priceDataBuilder = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(ChargeRequest.Currency.EUR.getValue())
                .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.INCLUSIVE)
                .setUnitAmount(product.getPrice())
                .setProductData(productData);

        SessionCreateParams.Mode mode = SessionCreateParams.Mode.valueOf(chargeRequestVM.getMode().toUpperCase());

        if (mode.equals(SessionCreateParams.Mode.SUBSCRIPTION)) {
            SessionCreateParams.LineItem.PriceData.Recurring recurring = SessionCreateParams.LineItem.PriceData.Recurring.builder()
                    .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.valueOf(chargeRequestVM.getRecurringInterval().toUpperCase()))
                    .build();

            priceDataBuilder.setRecurring(recurring);
        }

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceDataBuilder.build())
                .build();

        SessionCreateParams.AutomaticTax automaticTax = SessionCreateParams.AutomaticTax.builder()
                .setEnabled(true)
                .build();

        String originURL = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(mode)
                .setSuccessUrl(originURL + "/payment/success")
                .setCancelUrl(originURL + "/payment/cancel")
                .setLocale(SessionCreateParams.Locale.FR)
                .setAutomaticTax(automaticTax)
                .addLineItem(lineItem);


        if (EmailValidator.validateEmail(user.getEmail()))
            sessionBuilder.setCustomerEmail(user.getEmail());

        SessionCreateParams sessionCreateParams = sessionBuilder.build();

        try {
            Session session = Session.create(sessionCreateParams);
            return session.getUrl();
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

        if ("checkout.session.completed".equals(event.getType())) {
            Session prematureSession = (Session) event.getDataObjectDeserializer().getObject()
                    .orElseThrow(() -> new IllegalArgumentException(INVALID_PAYLOAD));
            SessionRetrieveParams params = SessionRetrieveParams.builder()
                    .addExpand("line_items")
                    .build();

            Session session = Session.retrieve(prematureSession.getId(), params, null);

            SessionListLineItemsParams listLineItemsParams = SessionListLineItemsParams.builder()
                    .addExpand("data.price.product")
                    .build();

            LineItemCollection lineItems = session.listLineItems(listLineItemsParams);
            Session.CustomerDetails customerDetails = session.getCustomerDetails();
            log.info("Customer email: {}", customerDetails != null ? customerDetails.getEmail() : "no customer details provided");


            for (LineItem lineItem : lineItems.getData()) {
                log.info("Line item: {}", lineItem.toString().replace("\n", " "));

                Price price = lineItem.getPrice();
                log.info("Price: {}", price.toString().replace("\n", " "));

                com.stripe.model.Product productObject = price.getProductObject();
                if (productObject != null) {
                    log.info("Product object: {}", productObject.toString().replace("\n", " "));

                    long productId = Long.parseLong(productObject.getMetadata().get(PRODUCT_ID));
                    log.info("Product id: {}", productId);

                    long userId = Long.parseLong(productObject.getMetadata().get(USER_ID));
                    log.info("User id: {}", userId);
                    subscribe.execute(userId, productId);
                }
            }
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

}
