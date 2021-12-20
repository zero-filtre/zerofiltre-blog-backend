package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;

public class PublishArticleException extends BlogException {


    public PublishArticleException(String message) {
        super(message);
    }
}
