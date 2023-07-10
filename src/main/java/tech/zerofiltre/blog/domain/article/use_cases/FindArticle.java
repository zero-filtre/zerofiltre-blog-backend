package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.metrics.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import static tech.zerofiltre.blog.domain.article.model.Status.*;

public class FindArticle {

    private final ArticleProvider articleProvider;
    private final MetricsProvider metricsProvider;
    private final ArticleViewProvider articleViewProvider;

    public FindArticle(ArticleProvider articleProvider, MetricsProvider metricsProvider, ArticleViewProvider articleViewProvider) {
        this.articleProvider = articleProvider;
        this.metricsProvider = metricsProvider;
        this.articleViewProvider = articleViewProvider;
    }

    public Article byId(long id, User viewer) throws ResourceNotFoundException {
        Article result = articleProvider.articleOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException("The article with id: " + id + " does not exist", String.valueOf(id), Domains.ARTICLE.name()));

        CounterSpecs counterSpecs = new CounterSpecs();
        counterSpecs.setName(CounterSpecs.ZEROFILTRE_ARTICLE_VIEWS);
        counterSpecs.setTags("status", result.getStatus().toString(), "title", result.getTitle(), "user", viewer != null ? viewer.getFullName() : "");
        metricsProvider.incrementCounter(counterSpecs);

        if (PUBLISHED.equals(result.getStatus()) && (viewer == null || result.getAuthor().getId() != viewer.getId())) {
            result.incrementViewsCount();
            result = articleProvider.save(result);
            ArticleView articleView = new ArticleView(viewer, result);
            articleViewProvider.save(articleView);
        }
        return result;

    }

    public Page<Article> of(FinderRequest request) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = request.getUser();

        //UNAUTHENTICATED USER TRYING TO GET NON PUBLISHED ARTICLES
        if (!PUBLISHED.equals(request.getStatus())
                && user == null
                && !request.isYours()) {
            throw new UnAuthenticatedActionException("The user token might be expired, try to refresh it. ", Domains.ARTICLE.name());
        }

        //NON ADMIN USER TRYING TO GET NON PUBLISHED ARTICLES
        if (!PUBLISHED.equals(request.getStatus())
                && (user == null || !user.getRoles().contains("ROLE_ADMIN"))
                && !request.isYours()) {
            throw new ForbiddenActionException("You are not authorize to request articles other than the published ones with this API. " +
                    "Please request with status=published or try /user/* API resources", Domains.ARTICLE.name());
        }

        long authorId = request.isYours() ? request.getUser().getId() : 0;
        return articleProvider.articlesOf(request.getPageNumber(), request.getPageSize(), request.getStatus(), authorId, request.getFilter(), request.getTag());

    }
}
