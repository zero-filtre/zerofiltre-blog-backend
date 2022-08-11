package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class FindArticle {

    private final ArticleProvider articleProvider;

    public FindArticle(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;
    }

    public Article byId(long id) throws ResourceNotFoundException {
        Article result = articleProvider.articleOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException("The article with id: " + id + " does not exist", String.valueOf(id), Domains.ARTICLE.name()));

        result.incrementViewsCount();
        result = articleProvider.save(result);
        return result;

    }

    public Page<Article> of(FindArticleRequest request) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = request.getUser();

        //UNAUTHENTICATED USER TRYING TO GET NON PUBLISHED ARTICLES
        if (!Status.PUBLISHED.equals(request.getStatus())
                && user == null
                && !request.isYours()) {
            throw new UnAuthenticatedActionException("The user token might be expired, try to refresh it. ", Domains.ARTICLE.name());
        }

        //NON ADMIN USER TRYING TO GET NON PUBLISHED ARTICLES
        if (!Status.PUBLISHED.equals(request.getStatus())
                && (user == null || !user.getRoles().contains("ROLE_ADMIN"))
                && !request.isYours()) {
            throw new ForbiddenActionException("You are not authorize to request articles other than the published ones with this API. " +
                    "Please request with status=published or try /user/* API resources", Domains.ARTICLE.name());
        }

        long authorId = request.isYours() ? request.getUser().getId() : 0;
        return articleProvider.articlesOf(request.getPageNumber(), request.getPageSize(), request.getStatus(), authorId, request.getFilter(),request.getTag());

    }
}
