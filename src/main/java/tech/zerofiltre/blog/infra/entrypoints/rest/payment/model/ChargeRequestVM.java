package tech.zerofiltre.blog.infra.entrypoints.rest.payment.model;

import lombok.Data;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;

import javax.validation.constraints.*;

@Data
public class ChargeRequestVM {

    @Min(value = 1, message = "The product id must be greater than 0")
    long productId;

    @NotNull(message = "The product type must not be null")
    ChargeRequest.ProductType productType;

    ChargeRequest.Currency currency;

    String paymentEmail;


    @NotNull(message = "The mode must not be null")
    @NotEmpty(message = "The mode must not be empty")
    @Pattern(regexp = "payment|subscription", message = "The mode must be either payment, subscription")
    String mode;

    boolean proPlan;

    @Pattern(regexp = "month|year", message = "The recurringInterval must be either month or year and requires proPlan to be true")
    String recurringInterval;

    @AssertTrue(message = "The mode must be either payment or subscription (with a recurringInterval = 'month' or 'year'; note that using 'recurringInterval' requires proPlan to be true). \n If you use xaf for payment then you must provide a valid paymentEmail address.")
    private boolean isOk() {
        return mode.equals("payment") // one shot payment
                || (mode.equals("subscription") && !proPlan)  // pay in 3
                || (mode.equals("subscription") && recurringInterval != null && currency == null) // pro plan eur
                || (mode.equals("subscription") && recurringInterval != null && ChargeRequest.Currency.XAF.equals(currency)
                && EmailValidator.validateEmail(paymentEmail)); // Mobile Money pro plan
    }

}
