package tech.zerofiltre.blog.domain.user.model;

import tech.zerofiltre.blog.domain.article.model.Article;

import java.util.Locale;

public class UserActionEvent {
    private String appUrl;
    private Locale locale;
    private User user;
    private Article article;
    private String currentToken;
    private Action action;


    public UserActionEvent(String appUrl, Locale locale, User user, String currentToken, Article article, Action action) {
        this.appUrl = appUrl;
        this.locale = locale;
        this.user = user;
        this.article = article;
        this.currentToken = currentToken;
        this.action = action;
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

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getCurrentToken() {
        return currentToken;
    }

}
