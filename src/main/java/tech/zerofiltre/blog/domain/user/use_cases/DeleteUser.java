package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.logging.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class DeleteUser {

    private final UserProvider userProvider;
    private final ArticleProvider articleProvider;
    private final VerificationTokenProvider tokenProvider;
    private final ReactionProvider reactionProvider;
    private final LoggerProvider loggerProvider;

    public DeleteUser(UserProvider userProvider, ArticleProvider articleProvider, VerificationTokenProvider tokenProvider, ReactionProvider reactionProvider, LoggerProvider loggerProvider) {
        this.userProvider = userProvider;
        this.articleProvider = articleProvider;
        this.tokenProvider = tokenProvider;
        this.reactionProvider = reactionProvider;
        this.loggerProvider = loggerProvider;
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
            LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting the user " + userIdToDelete + " for done", null, DeleteUser.class);
            loggerProvider.log(logEntry);
        } else {
            foundUser.setExpired(true);
            userProvider.save(foundUser);
            LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deactivating the user as it has articles", null, DeleteUser.class);
            loggerProvider.log(logEntry);

        }


    }
}
