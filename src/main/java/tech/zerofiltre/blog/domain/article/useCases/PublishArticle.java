package tech.zerofiltre.blog.domain.article.useCases;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;

import java.time.*;

public class PublishArticle {

    private final ArticleProvider articleProvider;

    public PublishArticle(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;
    }

    public Article execute(Article article) {
        LocalDateTime now = LocalDateTime.now();
        article.setStatus(Status.PUBLISHED);
        if (article.getPublishedAt() == null)
            article.setPublishedAt(now);
        article.setLastPublishedAt(now);
        return articleProvider.save(article);
    }
}
