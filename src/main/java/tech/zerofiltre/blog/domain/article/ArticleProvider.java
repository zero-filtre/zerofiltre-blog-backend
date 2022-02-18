package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public interface ArticleProvider {

    Optional<Article> articleOfId(long articleId);

    Article save(Article article);

    List<Article> articlesOf(int pageNumber, int pageSize, Status status, long authorId);

    List<Article> articlesOf(User user);
}
