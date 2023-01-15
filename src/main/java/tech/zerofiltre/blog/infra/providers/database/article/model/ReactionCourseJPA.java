package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reaction_course")
@EqualsAndHashCode(callSuper = true, exclude = {"course"})
public class ReactionCourseJPA extends BaseEntityJPA {

    private Reaction.Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CourseJPA course;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserJPA author;

}

