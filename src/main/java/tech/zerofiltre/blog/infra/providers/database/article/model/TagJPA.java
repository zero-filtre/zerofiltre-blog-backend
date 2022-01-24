package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tag")
public class TagJPA extends BaseEntityJPA {

    @ManyToMany(mappedBy = "tags")
    private List<ArticleJPA> articles;

    private String name;

    private String colorCode;

}
