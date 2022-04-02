package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class DeleteUser {

    private final UserProvider userProvider;
    private final ArticleProvider articleProvider;
    private final VerificationTokenProvider tokenProvider;
    private final ReactionProvider reactionProvider;

    public DeleteUser(UserProvider userProvider, ArticleProvider articleProvider, VerificationTokenProvider tokenProvider, ReactionProvider reactionProvider) {
        this.userProvider = userProvider;
        this.articleProvider = articleProvider;
        this.tokenProvider = tokenProvider;
        this.reactionProvider = reactionProvider;
    }

    public void execute(User currentUser, long userIdToDelete) throws ResourceNotFoundException, ForbiddenActionException {

        User foundUser = userProvider.userOfId(userIdToDelete).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the user you want to delete", String.valueOf(userIdToDelete), Domains.USER.name()));

        if (!currentUser.getRoles().contains("ROLE_ADMIN") && currentUser.getId() != foundUser.getId())
            throw new ForbiddenActionException("You can only delete your own account", Domains.USER.name());

        List<Article> userArticles = articleProvider.articlesOf(foundUser);
        if (userArticles.isEmpty()) {
            tokenProvider.ofUser(foundUser).ifPresent(tokenProvider::delete);
            reactionProvider.ofUser(foundUser).forEach(reactionProvider::delete);
            userProvider.deleteUser(foundUser);
        } else {
            foundUser.setExpired(true);
            userProvider.save(foundUser);
        }


    }
}
