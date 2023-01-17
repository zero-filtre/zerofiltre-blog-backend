package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

public class Subscription {

    private long id;
    private User subscriber;
    private Course course;
    private boolean completed;
    private boolean active = true;
    private LocalDateTime subscribedAt = LocalDateTime.now();
    private LocalDateTime suspendedAt;


    //TODO handle completed lessons
    private List<Lesson> completedLessons = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<Lesson> getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(List<Lesson> completedLessons) {
        this.completedLessons = completedLessons;
    }

    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(LocalDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }

    public void setSuspendedAt(LocalDateTime suspendedAt) {
        this.suspendedAt = suspendedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
