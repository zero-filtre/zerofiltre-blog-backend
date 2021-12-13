package tech.zerofiltre.blog.domain.article.useCases;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;

public class PublishArticle {

    private final ArticleProvider articleProvider;
    private final UserProvider userProvider;
    private final TagProvider tagProvider;

    public PublishArticle(ArticleProvider articleProvider, UserProvider userProvider, TagProvider tagProvider) {
        this.articleProvider = articleProvider;
        this.userProvider = userProvider;
        this.tagProvider = tagProvider;
    }

    public Article execute(Article article){

        return article;
    }
}
