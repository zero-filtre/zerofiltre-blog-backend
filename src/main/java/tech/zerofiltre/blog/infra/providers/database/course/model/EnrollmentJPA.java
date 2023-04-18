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
@Table(name = "enrollment", uniqueConstraints = {@UniqueConstraint(name = "UniqueUserPerCourseId", columnNames = {"user_id", "course_id"})})
@EqualsAndHashCode(callSuper = true)
public class EnrollmentJPA extends BaseEntityJPA {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id")
    private UserJPA user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "course_id")
    private CourseJPA course;
    private boolean completed;
    private boolean active;
    private LocalDateTime enrolledAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime lastModifiedAt;
    private String plan;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "enrollment_completed_lessons",
            joinColumns = @JoinColumn(name = "enrollment_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id")
    )
    private Set<LessonJPA> completedLessons;


}
