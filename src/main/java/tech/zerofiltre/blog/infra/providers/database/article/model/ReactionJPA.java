package tech.zerofiltre.blog.infra.providers.database.article.model;

import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reaction")
public class ReactionJPA extends BaseEntityJPA {

    private Reaction.Action action;

    private long articleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserJPA author;

}

