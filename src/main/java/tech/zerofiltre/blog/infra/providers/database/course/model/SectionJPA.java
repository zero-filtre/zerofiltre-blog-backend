package tech.zerofiltre.blog.infra.providers.database.course.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "section")
@EqualsAndHashCode(callSuper = true, exclude = {"course"})
public class SectionJPA extends BaseEntityJPA {

    private int position;
    private String title;
    @Lob
    private String content;
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CourseJPA course;
}
