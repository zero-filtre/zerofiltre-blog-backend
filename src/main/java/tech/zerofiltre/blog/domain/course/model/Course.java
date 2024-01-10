package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class Course extends Product {
    private List<Tag> tags = new ArrayList<>();
    private long enrolledCount;
    private Status status = Status.DRAFT;

    private User author;
    private String video;
    private List<Section> sections = new ArrayList<>();
    private List<Reaction> reactions = new ArrayList<>();
    private int lessonsCount;
    private Sandbox.Type sandboxType = Sandbox.Type.NONE;
    private boolean mentored;


    public Sandbox.Type getSandboxType() {
        return sandboxType;
    }

    public void setSandboxType(Sandbox.Type sandboxType) {
        this.sandboxType = sandboxType;
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


    public String getVideo() {
        return video;
    }

    public List<Section> getSections() {
        return sections;
    }

    public Status getStatus() {
        return status;
    }


    public List<Reaction> getReactions() {
        return reactions;
    }

    public int getLessonsCount() {
        return lessonsCount;
    }


    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setEnrolledCount(long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }


    public void setStatus(Status status) {
        this.status = status;
    }


    public void setAuthor(User author) {
        this.author = author;
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


    public boolean isMentored() {
        return mentored;
    }

    public void setMentored(boolean mentored) {
        this.mentored = mentored;
    }
}
