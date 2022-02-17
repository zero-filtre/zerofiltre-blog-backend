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


    public Article execute(User currentEditor, long id, String title, String thumbnail, String summary, String content, List<Tag> tags, Status status) throws PublishOrSaveArticleException, ForbiddenActionException {

        LocalDateTime now = LocalDateTime.now();

        Article existingArticle = articleProvider.articleOfId(id)
                .orElseThrow(() -> new PublishOrSaveArticleException("We can not publish/update an unknown article. Could not find an article with id: " + id, id));

        User author = existingArticle.getAuthor();
        if (!currentEditor.getEmail().equals(author.getEmail()) && !currentEditor.getRoles().contains("ROLE_ADMIN"))
            throw new ForbiddenActionException("You are not allowed to edit this article", Domains.ARTICLE.name());

        checkTags(tags);
        existingArticle.setTags(tags);
        existingArticle.setTitle(title);
        existingArticle.setThumbnail(thumbnail);
        existingArticle.setContent(content);
        existingArticle.setSummary(summary);
        if (existingArticle.getStatus().equals(Status.DRAFT))
            existingArticle.setStatus(status);
        existingArticle.setLastSavedAt(now);

        if (status.equals(Status.PUBLISHED)) {
            if (existingArticle.getPublishedAt() == null)
                existingArticle.setPublishedAt(now);
            existingArticle.setLastPublishedAt(now);
        }


        return articleProvider.save(existingArticle);

    }

    private void checkTags(List<Tag> tags) throws PublishOrSaveArticleException {
        for (Tag tag : tags) {
            if (tagProvider.tagOfId(tag.getId()).isEmpty())
                throw new PublishOrSaveArticleException("We can not publish the article. Could not find the related tag with id: " + tag.getId(), tag.getId());
        }
    }
}
