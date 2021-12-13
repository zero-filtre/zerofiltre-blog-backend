package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import javax.persistence.*;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class TagJPA extends BaseEntity {

    @ManyToMany(
            fetch = FetchType.LAZY,
            mappedBy = "tags")
    private List<ArticleJPA> articles;
}
