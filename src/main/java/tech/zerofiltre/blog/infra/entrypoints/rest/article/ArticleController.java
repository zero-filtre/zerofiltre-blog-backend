package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import lombok.extern.slf4j.*;
import org.springframework.context.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;

import javax.servlet.http.*;
import javax.validation.*;
import javax.validation.constraints.*;

@Slf4j
@RestController
@RequestMapping("/article")
public class ArticleController {

    private final PublishOrSaveArticle publishOrSaveArticle;
    private final InitArticle initArticle;
    private final FindArticle findArticle;
    private final SecurityContextManager securityContextManager;
    private final DeleteArticle deleteArticle;
    private final MessageSource sources;

    public ArticleController(ArticleProvider articleProvider, MetricsProvider metricsProvider, TagProvider tagProvider, LoggerProvider loggerProvider, SecurityContextManager securityContextManager, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.sources = sources;
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider);
        initArticle = new InitArticle(articleProvider);
        findArticle = new FindArticle(articleProvider, metricsProvider);
        deleteArticle = new DeleteArticle(articleProvider, loggerProvider);
    }


    @GetMapping("/{id}")
    public Article articleById(@PathVariable("id") long articleId) throws ResourceNotFoundException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return the wanted article", e);
        }
        return findArticle.byId(articleId, user);
    }

    @GetMapping
    public Page<Article> articles(
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam String status,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String tag
    ) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return published articles", e);
        }


        FinderRequest request = new FinderRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setUser(user);
        request.setTag(tag);
        if (filter != null) {
            filter = filter.toUpperCase();
            request.setFilter(FinderRequest.Filter.valueOf(filter));
        }
        if (status != null) {
            status = status.toUpperCase();
            request.setStatus(Status.valueOf(status));
        }
        return findArticle.of(request);
    }

    @PatchMapping
    public Article save(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        return publishOrSaveArticle.execute(user, publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getSummary(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), Status.DRAFT);
    }

    @PatchMapping("/publish")
    public Article publish(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        return publishOrSaveArticle.execute(user, publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getSummary(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), Status.PUBLISHED);
    }

    @PostMapping
    public Article init(@RequestParam @NotNull @NotEmpty String title) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        return initArticle.execute(title, user);
    }

    @DeleteMapping("/{id}")
    public String deleteArticle(@PathVariable("id") long articleId, HttpServletRequest request) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        deleteArticle.execute(user, articleId);
        return sources.getMessage("message.delete.course.success", null, request.getLocale());
    }


}
