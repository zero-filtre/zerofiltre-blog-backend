package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.features.FindArticle;
import tech.zerofiltre.blog.domain.article.features.InitArticle;
import tech.zerofiltre.blog.domain.article.features.PublishOrSaveArticle;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.features.DeleteArticle;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.PublishOrSaveArticleVM;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
    private final String appUrl;
    private final InfraProperties infraProperties;

    public ArticleController(ArticleProvider articleProvider, MetricsProvider metricsProvider, TagProvider tagProvider, LoggerProvider loggerProvider, SecurityContextManager securityContextManager, MessageSource sources, ArticleViewProvider articleViewProvider, UserNotificationProvider notificationProvider, InfraProperties infraProperties) {
        this.securityContextManager = securityContextManager;
        this.sources = sources;
        this.infraProperties = infraProperties;
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider, notificationProvider);
        initArticle = new InitArticle(articleProvider);
        findArticle = new FindArticle(articleProvider, metricsProvider, articleViewProvider);
        deleteArticle = new DeleteArticle(articleProvider, loggerProvider);

        appUrl = ZerofiltreUtils.getOriginUrl(this.infraProperties.getEnv());
    }


    @GetMapping("/{id}")
    public Article articleById(@PathVariable("id") long articleId) throws ResourceNotFoundException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return the wanted article");
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
            log.debug("We did not find a connected user but we can still return published articles");
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
        return publishOrSaveArticle.execute(user, publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getSummary(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), publishOrSaveArticleVM.getVideo(), Status.DRAFT, appUrl);
    }

    @PatchMapping("/publish")
    public Article publish(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        return publishOrSaveArticle.execute(user, publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getSummary(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), publishOrSaveArticleVM.getVideo(), Status.PUBLISHED, appUrl);
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
