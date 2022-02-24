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
    private Status status;
    private List<Tag> tags = new ArrayList<>();
    private String summary;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;
        Article article = (Article) o;
        return getId() == article.getId() && Objects.equals(getTitle(), article.getTitle()) && Objects.equals(getThumbnail(), article.getThumbnail()) && Objects.equals(getContent(), article.getContent()) && Objects.equals(getAuthor(), article.getAuthor()) && Objects.equals(getCreatedAt(), article.getCreatedAt()) && Objects.equals(getPublishedAt(), article.getPublishedAt()) && Objects.equals(getLastPublishedAt(), article.getLastPublishedAt()) && Objects.equals(getLastSavedAt(), article.getLastSavedAt()) && Objects.equals(getReactions(), article.getReactions()) && getStatus() == article.getStatus() && Objects.equals(getTags(), article.getTags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getThumbnail(), getContent(), getAuthor(), getCreatedAt(), getPublishedAt(), getLastPublishedAt(), getLastSavedAt(), getReactions(), getStatus(), getTags());
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
                '}';
    }
}
