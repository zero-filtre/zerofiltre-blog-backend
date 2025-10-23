package tech.zerofiltre.blog.domain.company.model;

import lombok.Getter;
import lombok.Setter;
import tech.zerofiltre.blog.domain.course.model.Course;

@Getter
@Setter
public class CompanyCourse extends Course {

    private boolean exclusive;

    public CompanyCourse(Course course, boolean exclusive) {
        this.setId(course.getId());
        this.setSubTitle(course.getSubTitle());
        this.setSummary(course.getSummary());
        this.setThumbnail(course.getThumbnail());
        this.setSandboxType(course.getSandboxType());
        this.setTags(course.getTags());
        this.setEnrolledCount(course.getEnrolledCount());
        this.setTitle(course.getTitle());
        this.setStatus(course.getStatus());
        this.setCreatedAt(course.getCreatedAt());
        this.setPublishedAt(course.getPublishedAt());
        this.setLastPublishedAt(course.getLastPublishedAt());
        this.setLastSavedAt(course.getLastSavedAt());
        this.setAuthor(course.getAuthor());
        this.setPrice(course.getPrice());
        this.setVideo(course.getVideo());
        this.setSections(course.getSections());
        this.setReactions(course.getReactions());
        this.setMentored(course.isMentored());

        this.exclusive = exclusive;
    }

}
