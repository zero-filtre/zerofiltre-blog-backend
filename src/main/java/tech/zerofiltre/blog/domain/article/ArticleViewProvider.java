package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.article.model.ArticleView;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleViewProvider {

    List<ArticleView> viewsOfArticle(long articleId);

    ArticleView save(ArticleView articleView);

    List<ArticleView> viewsOfUser(long viewerId);

    void delete(ArticleView articleView);

    int countArticlesReadByDatesAndUser(LocalDateTime startDate, LocalDateTime endDate, long viewerId);
}
