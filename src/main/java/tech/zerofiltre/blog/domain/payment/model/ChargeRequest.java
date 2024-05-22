package tech.zerofiltre.blog.domain.payment.model;

public class ChargeRequest {


    private Currency currency = Currency.EUR;
    private long productId;
    private String mode;
    private ProductType productType;
    private boolean proPlan;
    private String recurringInterval;
    private String paymentEmail;


    public String getRecurringInterval() {
        return recurringInterval;
    }

    public void setRecurringInterval(String recurringInterval) {
        this.recurringInterval = recurringInterval;
    }

    public boolean isProPlan() {
        return proPlan;
    }

    public void setProPlan(boolean proPlan) {
        this.proPlan = proPlan;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }


    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }


    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public String getPaymentEmail() {
        return paymentEmail;
    }

    public void setPaymentEmail(String paymentEmail) {
        this.paymentEmail = paymentEmail;
    }

    public enum Currency {
        EUR("eur"),
        XAF("XAF");

        private final String value;

        Currency(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum ProductType {
        COURSE, BOOTCAMP, MENTORED
    }
}
