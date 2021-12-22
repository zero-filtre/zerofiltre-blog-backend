package tech.zerofiltre.blog.domain.article.model;


import tech.zerofiltre.blog.domain.user.model.*;

public class Reaction {

    private long id;
    private Action action;
    private Article article;
    private User author;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }


    public enum Action {
        LOVE,
        FIRE,
        LIKE,
        CLAP
    }


}

