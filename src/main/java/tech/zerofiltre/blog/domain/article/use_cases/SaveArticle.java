package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;

public class SaveArticle {

    private final ArticleProvider articleProvider;
    private final UserProvider userProvider;
    private final TagProvider tagProvider;

    public SaveArticle(ArticleProvider articleProvider, UserProvider userProvider, TagProvider tagProvider) {
        this.articleProvider = articleProvider;
        this.userProvider = userProvider;
        this.tagProvider = tagProvider;
    }


    public Article execute(Article article) throws SaveArticleException {
        LocalDateTime now = LocalDateTime.now();
        checkTags(article);
        checkAuthor(article);
        if (article.getCreatedAt() == null) {
            article.setCreatedAt(now);
        }
        article.setLastSavedAt(now);
        article.setStatus(Status.DRAFT);
        return articleProvider.save(article);
    }

    private void checkAuthor(Article article) throws SaveArticleException {
        User user = article.getAuthor();
        if (user != null) {
            if (userProvider.userOfId(user.getId()).isEmpty())
                throw new SaveArticleException("Can not find a user with id " + user.getId());
        } else {
            throw new SaveArticleException("Can not publish and article with an unknown author");
        }
    }

    private void checkTags(Article article) throws SaveArticleException {
        for (Tag tag : article.getTags()) {
            if (tagProvider.tagOfId(tag.getId()).isEmpty())
                throw new SaveArticleException("Can not find a tag with id " + tag.getId());
        }
    }
}
