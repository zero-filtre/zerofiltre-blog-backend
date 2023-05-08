package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;

public class PublishOrSaveArticleException extends ZerofiltreException {

    private final long itemId;

    public PublishOrSaveArticleException(String message, long itemId) {
        super(message, Domains.ARTICLE.name());
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
    }
}
