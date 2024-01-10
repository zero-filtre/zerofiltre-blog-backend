package tech.zerofiltre.blog.infra.providers.notification.user.model;

import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;

public class ArticleSubmittedEvent extends ActionApplicationEvent {

    private Article article;

    public ArticleSubmittedEvent(User user, Locale locale, String appUrl, Article article) {
        super(user, locale, appUrl, null, Action.ARTICLE_SUBMITTED);
        this.article = article;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
