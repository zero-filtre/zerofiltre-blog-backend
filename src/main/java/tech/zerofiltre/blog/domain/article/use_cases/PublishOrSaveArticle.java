package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

public class PublishOrSaveArticle {

    private final ArticleProvider articleProvider;
    private final TagProvider tagProvider;

    public PublishOrSaveArticle(ArticleProvider articleProvider, TagProvider tagProvider) {
        this.articleProvider = articleProvider;
        this.tagProvider = tagProvider;
    }


    public Article execute(User currentEditor, long articleId, String title, String thumbnail, String summary, String content, List<Tag> tags, Status status) throws PublishOrSaveArticleException, ForbiddenActionException {

        LocalDateTime now = LocalDateTime.now();

        Article existingArticle = articleProvider.articleOfId(articleId)
                .orElseThrow(() -> new PublishOrSaveArticleException("We can not publish/update an unknown article. Could not find an article with id: " + articleId, articleId));

        User author = existingArticle.getAuthor();
        if (!isAuthor(currentEditor, author) && !isAdmin(currentEditor))
            throw new ForbiddenActionException("You are not allowed to edit this article", Domains.ARTICLE.name());

        checkTags(tags);
        existingArticle.setTags(tags);
        existingArticle.setTitle(title);
        existingArticle.setThumbnail(thumbnail);
        existingArticle.setContent(content);
        existingArticle.setSummary(summary);
        existingArticle.setLastSavedAt(now);

        if (!isAlreadyPublished(existingArticle) && isTryingToPublish(status) && !isAdmin(currentEditor))
            existingArticle.setStatus(Status.IN_REVIEW);

        if (!isAlreadyPublished(existingArticle) && (!isTryingToPublish(status) || isAdmin(currentEditor)))
            existingArticle.setStatus(status);


        //If article has been published few lines upper, check if the published date is set, otherwise do it.
        if (isAlreadyPublished(existingArticle)) {
            if (existingArticle.getPublishedAt() == null)
                existingArticle.setPublishedAt(now);
            existingArticle.setLastPublishedAt(now);
        }

        return articleProvider.save(existingArticle);
    }


    private boolean isAlreadyPublished(Article existingArticle) {
        return existingArticle.getStatus().equals(Status.PUBLISHED);
    }

    private boolean isTryingToPublish(Status status) {
        return status.equals(Status.PUBLISHED) || status.equals(Status.IN_REVIEW);
    }

    private boolean isAdmin(User currentEditor) {
        return currentEditor.getRoles().contains("ROLE_ADMIN");
    }

    private boolean isAuthor(User currentEditor, User author) {
        return currentEditor.getEmail().equals(author.getEmail());
    }

    private void checkTags(List<Tag> tags) throws PublishOrSaveArticleException {
        for (Tag tag : tags) {
            if (tagProvider.tagOfId(tag.getId()).isEmpty())
                throw new PublishOrSaveArticleException("We can not publish the article. Could not find the related tag with id: " + tag.getId(), tag.getId());
        }
    }
}
