package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;

public class ArticleNotFoundException extends BlogException {

    private final long articleId;

    public ArticleNotFoundException(String message, long articleId) {
        super(message);
        this.articleId = articleId;
    }

    public long getArticleId() {
        return articleId;
    }
}
