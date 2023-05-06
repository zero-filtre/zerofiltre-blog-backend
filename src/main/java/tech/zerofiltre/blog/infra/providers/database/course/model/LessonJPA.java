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
@Table(name = "lesson")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true, exclude = {"resources", "chapter"})
public class LessonJPA extends BaseEntityJPA {

    private String title;
    @Lob
    private String content;
    private String summary;
    private String thumbnail;
    private String video;
    private boolean free;
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private ChapterJPA chapter;

    private int number;

    @OrderBy("name ASC")
    @OneToMany(mappedBy = "lesson")
    private Set<ResourceJPA> resources;
}

