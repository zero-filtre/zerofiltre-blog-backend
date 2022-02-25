package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;

import java.util.*;

public class AddReaction {

    private final ArticleProvider articleProvider;

    public AddReaction(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;
    }


    public List<Reaction> execute(Reaction reaction) throws ResourceNotFoundException, ForbiddenActionException {
        long articleId = reaction.getArticleId();

        Article article = articleProvider.articleOfId(articleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We couldn't find the article you are trying to react on",
                        String.valueOf(articleId),
                        Domains.ARTICLE.name()));
        if (article.getStatus().compareTo(Status.PUBLISHED) < 0)
            throw new ForbiddenActionException("You can not react on an unpublished article", Domains.ARTICLE.name());

        List<Reaction> reactions = article.getReactions();
        long currentUserReactionsCount = reactions.stream()
                .filter(aReaction -> aReaction.getAuthorId() == reaction.getAuthorId())
                .count();
        if (currentUserReactionsCount >= 49)
            throw new ForbiddenActionException("You can not react on an article more that 50 times", Domains.ARTICLE.name());

        reactions.add(reaction);
        article = articleProvider.save(article);
        return article.getReactions();
    }
}
