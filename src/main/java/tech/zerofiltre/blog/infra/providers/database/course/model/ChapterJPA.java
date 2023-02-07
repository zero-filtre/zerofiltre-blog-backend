package tech.zerofiltre.blog.infra.providers.database.course.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chapter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true, exclude = {"lessons"})
public class ChapterJPA extends BaseEntityJPA {

    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CourseJPA course;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ChapterJPANumber number;

    @OrderBy("number ASC")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chapter")
    private Set<LessonJPA> lessons;
}
