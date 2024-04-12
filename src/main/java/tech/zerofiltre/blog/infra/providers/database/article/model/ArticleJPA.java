package tech.zerofiltre.blog.infra.providers.database.article.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "article")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class ArticleJPA extends BaseEntityJPA {

    private String title;
    private String thumbnail;
    @Lob
    private String content;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "author_id")
    private UserJPA author;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime lastSavedAt;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "article")
    private Set<ReactionArticleJPA> reactions;

    private Status status;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagJPA> tags;
    private String summary;
    private long viewsCount;
    private boolean premium;
}
