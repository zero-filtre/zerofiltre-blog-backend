package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArticleProvider {

    Optional<Article> articleOfId(long articleId);

    Article save(Article article);

    Page<Article> articlesOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag);

    List<Article> articlesOf(User user);

    List<Article> newArticlesFromLastMonth();

    void delete(Article article);

    int countPublishedArticlesByDatesAndUser(LocalDateTime startDate, LocalDateTime endDate, long authorId);
}
