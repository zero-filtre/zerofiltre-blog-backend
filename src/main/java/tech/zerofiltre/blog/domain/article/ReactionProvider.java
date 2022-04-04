package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public interface ReactionProvider {

    Optional<Reaction> reactionOfId(long reactionId);

    List<Reaction> reactions();

    List<Reaction> ofUser(User user);

    Reaction save(Reaction reaction);

    void delete(Reaction reaction);
}
