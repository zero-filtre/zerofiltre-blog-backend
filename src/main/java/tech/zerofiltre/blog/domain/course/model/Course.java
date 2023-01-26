package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

public class Course {
    private long id;
    private String subTitle;
    private String summary;
    private String thumbnail;
    private List<Tag> tags = new ArrayList<>();
    private long enrolledCount;
    private String title;
    private Status status = Status.DRAFT;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime lastSavedAt;
    private User author;
    private double price;
    private String video;
    private List<Section> sections = new ArrayList<>();
    private List<Reaction> reactions = new ArrayList<>();
    private int lessonsCount;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getSummary() {
        return summary;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public User getAuthor() {
        return author;
    }

    public double getPrice() {
        return price;
    }

    public String getVideo() {
        return video;
    }

    public List<Section> getSections() {
        return sections;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getLastPublishedAt() {
        return lastPublishedAt;
    }

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }

    public int getLessonsCount() {
        return lessonsCount;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setEnrolledCount(long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setLastPublishedAt(LocalDateTime lastPublishedAt) {
        this.lastPublishedAt = lastPublishedAt;
    }

    public void setLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public void setReactions(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    public void setLessonsCount(int lessonsCount) {
        this.lessonsCount = lessonsCount;
    }
}
