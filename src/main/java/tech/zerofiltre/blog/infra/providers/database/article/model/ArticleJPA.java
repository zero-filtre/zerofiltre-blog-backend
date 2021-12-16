package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import javax.persistence.*;
import java.time.*;
import java.util.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ArticleJPA extends BaseEntity {

    private String title;
    private String thumbnail;
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserJPA author;
    private LocalDateTime publishedAt;

    @ElementCollection
    @CollectionTable(name = "reactions", joinColumns = @JoinColumn(name = "article_id"))
    private List<Reaction> reactions;

    private Status status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagJPA> tags;
}
