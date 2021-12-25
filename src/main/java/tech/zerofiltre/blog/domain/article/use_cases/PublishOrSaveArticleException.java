package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;

public class PublishOrSaveArticleException extends BlogException {


    public PublishOrSaveArticleException(String message) {
        super(message);
    }
}
