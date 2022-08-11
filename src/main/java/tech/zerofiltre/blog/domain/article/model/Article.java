package tech.zerofiltre.blog.domain.article.model;

import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

public class Article {
    private long id;
    private String title = "Donnez-moi un titre!";
    private String thumbnail;
    private String content;
    private User author;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime lastSavedAt;
    private List<Reaction> reactions = new ArrayList<>();
    private Status status = Status.DRAFT;
    private List<Tag> tags = new ArrayList<>();
    private String summary;
    private long viewsCount;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public LocalDateTime getLastPublishedAt() {
        return lastPublishedAt;
    }

    public void setLastPublishedAt(LocalDateTime lastPublishedAt) {
        this.lastPublishedAt = lastPublishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public void setLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public void incrementViewsCount() {
        this.viewsCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return id == article.id && viewsCount == article.viewsCount && Objects.equals(title, article.title) && Objects.equals(thumbnail, article.thumbnail) && Objects.equals(content, article.content) && Objects.equals(author, article.author) && Objects.equals(createdAt, article.createdAt) && Objects.equals(publishedAt, article.publishedAt) && Objects.equals(lastPublishedAt, article.lastPublishedAt) && Objects.equals(lastSavedAt, article.lastSavedAt) && Objects.equals(reactions, article.reactions) && status == article.status && Objects.equals(tags, article.tags) && Objects.equals(summary, article.summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, thumbnail, content, author, createdAt, publishedAt, lastPublishedAt, lastSavedAt, reactions, status, tags, summary, viewsCount);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", content='" + content + '\'' +
                ", author=" + author +
                ", createdAt=" + createdAt +
                ", publishedAt=" + publishedAt +
                ", lastPublishedAt=" + lastPublishedAt +
                ", lastSavedAt=" + lastSavedAt +
                ", reactions=" + reactions +
                ", status=" + status +
                ", tags=" + tags +
                ", summary='" + summary + '\'' +
                ", viewsCount=" + viewsCount +
                '}';
    }
}
