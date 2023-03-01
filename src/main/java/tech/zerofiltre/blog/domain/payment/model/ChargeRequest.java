package tech.zerofiltre.blog.domain.payment.model;

public class ChargeRequest {

    private final Currency currency = Currency.EUR;
    private long productId;
    private String description;
    private int amount;
    private String email;
    private String token;
    private ProductType productType;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public enum Currency {
        EUR("eur");

        private final String value;

        Currency(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum ProductType {
        COURSE, BOOTCAMP
    }
}
