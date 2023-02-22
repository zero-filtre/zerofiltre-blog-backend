package tech.zerofiltre.blog.infra.providers.database.course.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import javax.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subscription", uniqueConstraints = {@UniqueConstraint(name = "UniqueSubscriberPerCourseId", columnNames = {"subscriber_id", "course_id"})})
@EqualsAndHashCode(callSuper = true)
public class SubscriptionJPA extends BaseEntityJPA {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "subscriber_id")
    private UserJPA subscriber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "course_id")
    private CourseJPA course;
    private boolean completed;
    private boolean active;
    private LocalDateTime subscribedAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime lastModifiedAt;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "subscription_completed_lessons",
            joinColumns = @JoinColumn(name = "subscription_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id")
    )
    private Set<LessonJPA> completedLessons;


}
