package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;
import tech.zerofiltre.blog.util.*;

import javax.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final PublishOrSaveArticle publishOrSaveArticle;
    private final InitArticle initArticle;


    public ArticleController(ArticleProvider articleProvider, TagProvider tagProvider) {
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider);
        initArticle = new InitArticle(articleProvider);
    }

    private final Article mockArticle = ZerofiltreUtils.createMockArticle(true);
    private final List<Article> mockArticles = new ArrayList<>();


    @PostConstruct
    void setup() {
        for (long i = 0; i < 20; i++) {
            mockArticle.setId(i + 1);
            mockArticle.setPublishedAt(LocalDateTime.now().minusDays(i));
            mockArticles.add(mockArticle);
        }

    }

    @GetMapping("/{id}")
    public Article articleById(@PathVariable("id") long articleId) {
        return mockArticle;
    }

    @GetMapping("/list")
    public List<Article> articleCards() {
        return mockArticles;
    }

    @PatchMapping
    public Article save(@RequestBody PublishOrSaveArticleVM publishOrSaveArticleVM) throws PublishOrSaveArticleException {
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
    public Article publish(@RequestBody PublishOrSaveArticleVM publishOrSaveArticleVM) throws PublishOrSaveArticleException {
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
    public Article init(@RequestParam String title) {
        User user = getAuthenticatedUser();
        return initArticle.execute(title, user);
    }

    private User getAuthenticatedUser() {
        //TODO get Authenticated user via Spring security APIs
        return ZerofiltreUtils.createMockUser();
    }

}
