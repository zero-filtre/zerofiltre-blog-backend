package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.logging.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class DeleteArticle {

    private final ArticleProvider articleProvider;
    private final LoggerProvider loggerProvider;

    public DeleteArticle(ArticleProvider articleProvider, LoggerProvider loggerProvider) {
        this.articleProvider = articleProvider;
        this.loggerProvider = loggerProvider;
    }

    public void execute(User currentUser, long articleIdToDelete) throws ResourceNotFoundException, ForbiddenActionException {


        Article foundArticle = articleProvider.articleOfId(articleIdToDelete).orElseThrow(() ->
                new ResourceNotFoundException("We couldn't find the article you want to delete", String.valueOf(articleIdToDelete), Domains.ARTICLE.name()));


        if (!currentUser.getRoles().contains("ROLE_ADMIN") && currentUser.getId() != foundArticle.getAuthor().getId())
            throw new ForbiddenActionException("You can only delete your own article", Domains.USER.name());

        articleProvider.delete(foundArticle);

        LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting article " + articleIdToDelete + " for done", null, DeleteArticle.class);
        loggerProvider.log(logEntry);
    }
}
