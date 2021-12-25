package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;

public class FindArticleException extends BlogException {


    public FindArticleException(String message) {
        super(message);
    }
}
