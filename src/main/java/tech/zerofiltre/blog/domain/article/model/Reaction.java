package tech.zerofiltre.blog.domain.article.model;


import tech.zerofiltre.blog.domain.user.model.*;

public class Reaction {

    private long id;
    private Action action;
    private User author;
    private long articleId;

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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public long getArticleId() {
        return articleId;
    }

    public void setArticleId(long articleId) {
        this.articleId = articleId;
    }


    public enum Action {
        LOVE,
        FIRE,
        LIKE,
        CLAP
    }


}

