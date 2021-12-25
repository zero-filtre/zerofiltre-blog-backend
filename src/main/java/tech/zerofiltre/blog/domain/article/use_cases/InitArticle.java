package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;

public class InitArticle {

    private final ArticleProvider articleProvider;


    public InitArticle(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;

    }


    public Article execute(String title, User user) {
        LocalDateTime now = LocalDateTime.now();
        Article article = new Article();
        article.setTitle(title);
        article.setCreatedAt(now);
        article.setLastSavedAt(now);
        article.setStatus(Status.DRAFT);
        article.setAuthor(user);
        return articleProvider.save(article);
    }

}
