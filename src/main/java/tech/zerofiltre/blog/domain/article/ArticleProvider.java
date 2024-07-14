package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.LocalDate;
import java.util.*;

public interface ArticleProvider {

    Optional<Article> articleOfId(long articleId);

    Article save(Article article);

    Page<Article> articlesOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag);

    List<Article> articlesOf(User user);

    void delete(Article article);

    int countPublishedArticlesByDatesAndUser(LocalDate startDate, LocalDate endDate, long authorId);
}
