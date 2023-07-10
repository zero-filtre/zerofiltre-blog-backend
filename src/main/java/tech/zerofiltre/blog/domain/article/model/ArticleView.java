package tech.zerofiltre.blog.domain.article.model;

import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;

public class ArticleView {

    private long id;
    private User viewer;
    private Article viewed;
    private LocalDateTime viewedAt;


    public ArticleView() {
    }

    public ArticleView(User viewer, Article viewed) {
        this.viewer = viewer;
        this.viewed = viewed;
        viewedAt = LocalDateTime.now();
    }

    public User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
    }

    public Article getViewed() {
        return viewed;
    }

    public void setViewed(Article viewed) {
        this.viewed = viewed;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
