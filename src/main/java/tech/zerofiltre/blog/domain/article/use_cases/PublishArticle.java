package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;

public class PublishArticle {

    private final ArticleProvider articleProvider;
    private final UserProvider userProvider;
    private final TagProvider tagProvider;

    public PublishArticle(ArticleProvider articleProvider, UserProvider userProvider, TagProvider tagProvider) {
        this.articleProvider = articleProvider;
        this.userProvider = userProvider;
        this.tagProvider = tagProvider;
    }


    public Article execute(Article article) throws PublishArticleException {
        LocalDateTime now = LocalDateTime.now();
        checkTags(article);
        checkAuthor(article);
        article.setStatus(Status.PUBLISHED);

        if (article.getCreatedAt() == null)
            article.setCreatedAt(now);
        article.setLastSavedAt(now);

        if (article.getPublishedAt() == null)
            article.setPublishedAt(now);
        article.setLastPublishedAt(now);

        return articleProvider.save(article);
    }

    private void checkAuthor(Article article) throws PublishArticleException {

        User user = article.getAuthor();
        if (user != null) {
            if (userProvider.userOfId(user.getId()).isEmpty())
                throw new PublishArticleException("Can not find a user with id " + user.getId());
        } else {
            throw new PublishArticleException("Can not publish and article with an unknown author");
        }
    }

    private void checkTags(Article article) throws PublishArticleException {
        for (Tag tag : article.getTags()) {
            if (tagProvider.tagOfId(tag.getId()).isEmpty())
                throw new PublishArticleException("Can not find a tag with id " + tag.getId());
        }
    }
}
