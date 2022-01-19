package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;

public class PublishOrSaveArticleException extends BlogException {

    private final long itemId;

    public PublishOrSaveArticleException(String message, long itemId) {
        super(message);
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
    }
}
