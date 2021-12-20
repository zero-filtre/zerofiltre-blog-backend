package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;

public class SaveArticleException extends BlogException {


    public SaveArticleException(String message) {
        super(message);
    }
}
