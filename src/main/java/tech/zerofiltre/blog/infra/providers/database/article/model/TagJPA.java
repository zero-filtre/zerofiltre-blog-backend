package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tag")
@EqualsAndHashCode(callSuper = true,exclude = "articles")
public class TagJPA extends BaseEntityJPA {

    @ManyToMany(mappedBy = "tags")
    private List<ArticleJPA> articles;

    @Column(unique = true)
    private String name;

    private String colorCode;

}
