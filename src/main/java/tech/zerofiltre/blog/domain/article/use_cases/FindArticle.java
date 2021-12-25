package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;

import java.util.*;

public class FindArticle {

    private final ArticleProvider articleProvider;

    public FindArticle(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;
    }

    public Article byId(long id) throws FindArticleException {
        return articleProvider.articleOfId(id)
                .orElseThrow(() -> new FindArticleException("We could not find an article with id: " + id));
    }

    public List<Article> of(int pageNumber, int pageSize) {
        return articleProvider.articlesOf(pageNumber, pageSize);

    }
}
