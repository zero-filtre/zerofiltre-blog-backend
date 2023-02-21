package tech.zerofiltre.blog.infra.providers.database.course.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "resource")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true, exclude = {"lesson"})
public class ResourceJPA extends BaseEntityJPA {
    private String type;
    private String url;
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private LessonJPA lesson;
}
