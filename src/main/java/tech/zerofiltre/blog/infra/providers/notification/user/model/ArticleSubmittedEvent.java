package tech.zerofiltre.blog.infra.providers.notification.user.model;

import org.springframework.context.ApplicationEvent;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;
import java.util.UUID;

public class ArticleSubmittedEvent extends ApplicationEvent {
    private String id;
    private String appUrl;
    private Locale locale;
    private User user;
    private Article article;
    private Action action;

    public ArticleSubmittedEvent(User user, Locale locale, String appUrl, Article article) {
        super(user);
        this.article = article;
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
        this.action = Action.ARTICLE_SUBMITTED;
    }

    public String getId() {
        return id;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
