package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class FindArticle {

    private final ArticleProvider articleProvider;

    public FindArticle(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;
    }

    public Article byId(long id) throws ArticleNotFoundException {
        return articleProvider.articleOfId(id)
                .orElseThrow(() -> new ArticleNotFoundException("The article with id: " + id + " does not exist", id));
    }

    public List<Article> of(FindArticleRequest request) throws ForbiddenActionException {
        User user = request.getUser();
        if (!Status.PUBLISHED.equals(request.getStatus()) && (user == null || !user.getRoles().contains("ROLE_ADMIN"))) {
            throw new ForbiddenActionException("You are not authorize to request articles other than the published ones with this API. " +
                    "Please request with status=published or try /user/* API resources");
        }
        return articleProvider.articlesOf(request.getPageNumber(), request.getPageSize(), request.getStatus());

    }
}
