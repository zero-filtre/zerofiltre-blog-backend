package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;
import tech.zerofiltre.blog.util.*;

import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final PublishOrSaveArticle publishOrSaveArticle;
    private final InitArticle initArticle;
    private final FindArticle findArticle;


    public ArticleController(ArticleProvider articleProvider, TagProvider tagProvider) {
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider);
        initArticle = new InitArticle(articleProvider);
        findArticle = new FindArticle(articleProvider);
    }


    @GetMapping("/{id}")
    public Article articleById(@PathVariable("id") long articleId) throws ArticleNotFoundException {
        return findArticle.byId(articleId);
    }

    @GetMapping("/list")
    public List<Article> articleCards(@RequestParam int pageNumber, @RequestParam int pageSize) {
        return findArticle.of(pageNumber, pageSize);
    }

    @PatchMapping
    public Article save(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws PublishOrSaveArticleException {
        return publishOrSaveArticle.execute(
                publishOrSaveArticleVM.getId(),
                publishOrSaveArticleVM.getTitle(),
                publishOrSaveArticleVM.getThumbnail(),
                publishOrSaveArticleVM.getContent(),
                publishOrSaveArticleVM.getTags(),
                Status.DRAFT
        );
    }

    @PatchMapping("/publish")
    public Article publish(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws PublishOrSaveArticleException {
        return publishOrSaveArticle.execute(
                publishOrSaveArticleVM.getId(),
                publishOrSaveArticleVM.getTitle(),
                publishOrSaveArticleVM.getThumbnail(),
                publishOrSaveArticleVM.getContent(),
                publishOrSaveArticleVM.getTags(),
                Status.PUBLISHED
        );
    }

    @PostMapping
    public Article init(@RequestParam @NotNull @NotEmpty String title) {
        User user = getAuthenticatedUser();
        return initArticle.execute(title, user);
    }

    private User getAuthenticatedUser() {
        //TODO get Authenticated user via Spring security APIs
        return ZerofiltreUtils.createMockUser();
    }

}
