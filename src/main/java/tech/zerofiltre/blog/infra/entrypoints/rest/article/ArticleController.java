package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.util.*;

import javax.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleProvider articleProvider;
    private final PublishArticle publishArticle;
    private final SaveArticle saveArticle;


    public ArticleController(ArticleProvider articleProvider, UserProvider userProvider, TagProvider tagProvider) {
        this.articleProvider = articleProvider;
        publishArticle = new PublishArticle(articleProvider, userProvider, tagProvider);
        saveArticle = new SaveArticle(articleProvider, userProvider, tagProvider);
    }

    private final Article mockArticle = ZerofiltreUtils.createMockArticle(true);
    private final List<Article> mockArticles = new ArrayList<>();


    @PostConstruct
    void setup() {
        for (int i = 0; i < 20; i++) {
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

    @PostMapping
    public Article save(@RequestBody Article article) throws SaveArticleException {
        return saveArticle.execute(article);
    }

    @PostMapping("/publish")
    public Article publish(@RequestBody Article article) throws PublishArticleException {
        return publishArticle.execute(article);
    }

    @PatchMapping
    public Article update(@RequestBody Article article) {
        return articleProvider.save(article);
    }
}
