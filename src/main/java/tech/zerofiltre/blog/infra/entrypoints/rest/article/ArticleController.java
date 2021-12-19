package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.useCases.*;
import tech.zerofiltre.blog.util.*;

import javax.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleProvider articleProvider;
    private final PublishArticle publishArticle;


    public ArticleController(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;
        publishArticle = new PublishArticle(articleProvider);
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
    public Article save(@RequestBody Article article) {
        return articleProvider.save(article);
    }

    @PostMapping("/publish")
    public Article publish(@RequestBody Article article) {
        return publishArticle.execute(article);
    }

    @PatchMapping
    public Article update(@RequestBody Article article) {
        return articleProvider.save(article);
    }
}
