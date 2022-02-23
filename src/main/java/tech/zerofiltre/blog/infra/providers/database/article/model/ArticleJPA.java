package tech.zerofiltre.blog.infra.providers.database.article.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import javax.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "article")
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private Set<ReactionJPA> reactions;

    private Status status;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagJPA> tags;
    private String summary;
}
