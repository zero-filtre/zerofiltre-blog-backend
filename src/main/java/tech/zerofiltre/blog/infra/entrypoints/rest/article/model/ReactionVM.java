package tech.zerofiltre.blog.infra.entrypoints.rest.article.model;

import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;

@Data
@ToString
public class ReactionVM {
    private Reaction.Action action;
    private long authorId;
    private long articleId;
    private long courseId;
}
