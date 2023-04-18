package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tag")
@EqualsAndHashCode(callSuper = true, exclude = {"articles","courses"})
public class TagJPA extends BaseEntityJPA {

    @ManyToMany(mappedBy = "tags")
    private List<ArticleJPA> articles;

    @ManyToMany(mappedBy = "tags")
    private List<CourseJPA> courses;
    @Column(unique = true)
    private String name;

    private String colorCode;

}
