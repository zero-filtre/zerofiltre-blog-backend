package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

public class Enrollment {

    private long id;
    private User user;
    private Course course;
    private boolean completed;
    private boolean active = true;
    private LocalDateTime enrolledAt = LocalDateTime.now();
    private LocalDateTime lastModifiedAt = LocalDateTime.now();
    private LocalDateTime suspendedAt;
    private User.Plan plan = User.Plan.BASIC;


    private List<Lesson> completedLessons = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
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

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public User.Plan getPlan() {
        return plan;
    }

    public void setPlan(User.Plan plan) {
        this.plan = plan;
    }
}
