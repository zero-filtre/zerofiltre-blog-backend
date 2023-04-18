package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reaction_article")
@EqualsAndHashCode(callSuper = true, exclude = {"article"})
public class ReactionArticleJPA extends BaseEntityJPA {

    private Reaction.Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private ArticleJPA article;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserJPA author;

}

