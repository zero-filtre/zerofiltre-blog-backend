package tech.zerofiltre.blog.domain.metrics.model;

public class CounterSpecs {
    public static final String ZEROFILTRE_ACCOUNT_CREATIONS = "zerofiltre_account_creations";
    public static final String ZEROFILTRE_ACCOUNT_CONNECTIONS = "zerofiltre_account_connections";
    public static final String ZEROFILTRE_CHECKOUT_CREATIONS = "zerofiltre_checkout_creations";
    public static final String ZEROFILTRE_ARTICLE_VIEWS = "zerofiltre_article_views";
    public static final String ZEROFILTRE_PASSWORD_RESETS = "zerofiltre_password_resets";

    String[] tags;
    String name;

    public String[] getTags() {
        return tags;
    }

    public void setTags(String... tags) {
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
