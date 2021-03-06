package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import lombok.extern.slf4j.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
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

    public ArticleController(ArticleProvider articleProvider, TagProvider tagProvider, LoggerProvider loggerProvider, SecurityContextManager securityContextManager, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.sources = sources;
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider);
        initArticle = new InitArticle(articleProvider);
        findArticle = new FindArticle(articleProvider);
        deleteArticle = new DeleteArticle(articleProvider, loggerProvider);
    }


    @GetMapping("/{id}")
    public Article articleById(@PathVariable("id") long articleId) throws ResourceNotFoundException {
        return findArticle.byId(articleId);
    }

    @GetMapping
    public Page<Article> articles(
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam String status,
            @RequestParam(required = false) boolean byPopularity,
            @RequestParam(required = false) String tag
    ) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (BlogException e) {
            log.trace("We did not find a connected user but we can still return published articles", e);
        }

        status = status.toUpperCase();
        FindArticleRequest request = new FindArticleRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setStatus(Status.valueOf(status));
        request.setUser(user);
        request.setTag(tag);
        request.setByPopularity(byPopularity);
        return findArticle.of(request);
    }

    @PatchMapping
    public Article save(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        return publishOrSaveArticle.execute(user, publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getSummary(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), Status.DRAFT);
    }

    @PatchMapping("/publish")
    public Article publish(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        return publishOrSaveArticle.execute(user, publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getSummary(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), Status.PUBLISHED);
    }

    @PostMapping
    public Article init(@RequestParam @NotNull @NotEmpty String title) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        return initArticle.execute(title, user);
    }

    @DeleteMapping("/{id}")
    public String deleteArticle(@PathVariable("id") long articleId, HttpServletRequest request) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        deleteArticle.execute(user, articleId);
        return sources.getMessage("message.delete.article.success", null, request.getLocale());
    }


}
