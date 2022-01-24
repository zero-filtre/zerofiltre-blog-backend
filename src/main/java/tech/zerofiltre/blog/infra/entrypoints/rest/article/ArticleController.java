package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;

import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final PublishOrSaveArticle publishOrSaveArticle;
    private final InitArticle initArticle;
    private final FindArticle findArticle;
    private final UserProvider userProvider;


    public ArticleController(ArticleProvider articleProvider, TagProvider tagProvider, UserProvider userProvider) {
        this.userProvider = userProvider;
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
        return publishOrSaveArticle.execute(publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), Status.DRAFT);
    }

    @PatchMapping("/publish")
    public Article publish(@RequestBody @Valid PublishOrSaveArticleVM publishOrSaveArticleVM) throws PublishOrSaveArticleException {
        return publishOrSaveArticle.execute(publishOrSaveArticleVM.getId(), publishOrSaveArticleVM.getTitle(), publishOrSaveArticleVM.getThumbnail(), publishOrSaveArticleVM.getContent(), publishOrSaveArticleVM.getTags(), Status.PUBLISHED);
    }

    @PostMapping
    public Article init(@RequestParam @NotNull @NotEmpty String title) throws BlogException {
        User user = getAuthenticatedUser();
        return initArticle.execute(title, user);
    }

    private User getAuthenticatedUser() throws BlogException {
        String userEmail = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userEmail = authentication.getName();
        }
        return userProvider.userOfEmail(userEmail)
                .orElseThrow(() -> new BlogException("No authenticated user found"));

    }

}
