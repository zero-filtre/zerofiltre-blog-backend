package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class Course extends Product {
    private List<Tag> tags = new ArrayList<>();
    private long enrolledCount;
    private Status status = Status.DRAFT;

    private User author;
    private String video;
    private List<Section> sections = new ArrayList<>();
    private List<Reaction> reactions = new ArrayList<>();
    private int lessonsCount;



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
}
