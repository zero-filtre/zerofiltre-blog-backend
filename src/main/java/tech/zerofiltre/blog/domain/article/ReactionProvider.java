package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.article.model.*;

import java.util.*;

public interface ReactionProvider {

    Optional<Reaction> reactionOfId(long reactionId);

    List<Reaction> reactions();

    Reaction save(Reaction reaction);
}
