package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;

import java.time.*;
import java.util.*;

public class PublishOrSaveArticle {

    private final ArticleProvider articleProvider;
    private final TagProvider tagProvider;

    public PublishOrSaveArticle(ArticleProvider articleProvider, TagProvider tagProvider) {
        this.articleProvider = articleProvider;
        this.tagProvider = tagProvider;
    }


    public Article execute(long id, String title, String thumbnail, String content, List<Tag> tags, Status status) throws PublishOrSaveArticleException {
        LocalDateTime now = LocalDateTime.now();

        Article existingArticle = articleProvider.articleOfId(id)
                .orElseThrow(() -> new PublishOrSaveArticleException("We can not publish an unknown article. Could not find an article with id: " + id));

        checkTags(tags);
        existingArticle.setTags(tags);
        existingArticle.setTitle(title);
        existingArticle.setThumbnail(thumbnail);
        existingArticle.setContent(content);
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
                throw new PublishOrSaveArticleException("Could not find a tag with id " + tag.getId());
        }
    }
}
