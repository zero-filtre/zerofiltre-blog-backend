package tech.zerofiltre.blog.infra.providers.database.article;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBArticleProvider.class, DBTagProvider.class})
class DBArticleProviderIT {

    @Autowired
    TagProvider tagProvider;
    @Autowired
    private ArticleJPARepository articleJPARepository;
    @Autowired
    private UserJPARepository userJPARepository;
    @Autowired
    private ReactionJPARepository reactionJPARepository;
    private ArticleJPAMapper articleJPAMapper = Mappers.getMapper(ArticleJPAMapper.class);

    private UserJPAMapper userJPAMapper = Mappers.getMapper(UserJPAMapper.class);

    @Autowired
    private ArticleProvider articleProvider;


    @BeforeEach
    void setUp() {
    }

    @Test
    void articlesOf_returnsMostReacted_InDescendingOrder() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        Article article0 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setAuthor(userJPA);
        articleJPARepository.save(article0JPA);

        ReactionJPA reaction0 = new ReactionJPA();
        reaction0.setAction(Reaction.Action.FIRE);
        reaction0.setArticle(article0JPA);
        reaction0.setAuthor(userJPA);

        ReactionJPA reaction1 = new ReactionJPA();
        reaction1.setAction(Reaction.Action.CLAP);
        reaction1.setArticle(article0JPA);
        reaction1.setAuthor(userJPA);

        ReactionJPA reaction2 = new ReactionJPA();
        reaction2.setAction(Reaction.Action.LOVE);
        reaction2.setArticle(article0JPA);
        reaction2.setAuthor(userJPA);

        article0JPA.setReactions(new HashSet<>(Arrays.asList(reaction0, reaction1, reaction2)));

        Article article1 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        ReactionJPA reaction3 = new ReactionJPA();
        reaction3.setAction(Reaction.Action.CLAP);
        reaction3.setAuthor(userJPA);
        reaction3.setArticle(article1JPA);

        ReactionJPA reaction4 = new ReactionJPA();
        reaction4.setAction(Reaction.Action.LOVE);
        reaction4.setAuthor(userJPA);
        reaction4.setArticle(article1JPA);


        article1JPA.setReactions(new HashSet<>(Arrays.asList(reaction3, reaction4)));


        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, 0, FindArticleRequest.Filter.POPULAR, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(3);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article0");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");
        assertThat(articles.getContent().get(2).getTitle()).isEqualTo("article2");

    }

    @Test
    void articlesOf_returnsMostReacted_InDescendingOrder_WithConnectedUser() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        //article0JPA has no author
        Article article0 = ZerofiltreUtils.createMockArticle(null, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article0JPA);

        ReactionJPA reaction0 = new ReactionJPA();
        reaction0.setAction(Reaction.Action.FIRE);
        reaction0.setArticle(article0JPA);
        reaction0.setAuthor(userJPA);

        ReactionJPA reaction1 = new ReactionJPA();
        reaction1.setAction(Reaction.Action.CLAP);
        reaction1.setArticle(article0JPA);
        reaction1.setAuthor(userJPA);

        ReactionJPA reaction2 = new ReactionJPA();
        reaction2.setAction(Reaction.Action.LOVE);
        reaction2.setArticle(article0JPA);
        reaction2.setAuthor(userJPA);

        article0JPA.setReactions(new HashSet<>(Arrays.asList(reaction0, reaction1, reaction2)));

        Article article1 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        ReactionJPA reaction3 = new ReactionJPA();
        reaction3.setAction(Reaction.Action.CLAP);
        reaction3.setAuthor(userJPA);
        reaction3.setArticle(article1JPA);

        ReactionJPA reaction4 = new ReactionJPA();
        reaction4.setAction(Reaction.Action.LOVE);
        reaction4.setAuthor(userJPA);
        reaction4.setArticle(article1JPA);


        article1JPA.setReactions(new HashSet<>(Arrays.asList(reaction3, reaction4)));


        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FindArticleRequest.Filter.POPULAR, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article1");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article2");

    }


    @Test
    void articlesOf_returnsMostViewed_InDescendingOrder() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        Article article0 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setAuthor(userJPA);
        article0JPA.setViewsCount(0);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setAuthor(userJPA);
        article1JPA.setViewsCount(2);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setViewsCount(3);
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, 0, FindArticleRequest.Filter.MOST_VIEWED, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(3);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");
        assertThat(articles.getContent().get(2).getTitle()).isEqualTo("article0");

    }

    @Test
    void articlesOf_returnsMostViewed_InDescendingOrder_WithConnectedUser() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        //article0JPA has no author
        Article article0 = ZerofiltreUtils.createMockArticle(null, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        article0JPA.setViewsCount(5);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        article1JPA.setViewsCount(2);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setViewsCount(3);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FindArticleRequest.Filter.MOST_VIEWED, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");

    }
    @Test
    void articlesOf_reliesOnTheProperFilter() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        //article1JPA has 2 reactions and 5 views
        Article article1 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        article1JPA.setViewsCount(5);
        articleJPARepository.save(article1JPA);

        ReactionJPA reaction1 = new ReactionJPA();
        reaction1.setAction(Reaction.Action.CLAP);
        reaction1.setAuthor(userJPA);
        reaction1.setArticle(article1JPA);

        ReactionJPA reaction2 = new ReactionJPA();
        reaction2.setAction(Reaction.Action.LOVE);
        reaction2.setAuthor(userJPA);
        reaction2.setArticle(article1JPA);
        article1JPA.setReactions(new HashSet<>(Arrays.asList(reaction1, reaction2)));


        //article1JPA has 0 reactions and 8 views
        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        article2JPA.setViewsCount(8);
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FindArticleRequest.Filter.MOST_VIEWED, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");

    }

    @Test
    void articlesOf_returnsSelectedTag_InDescendingOrder() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        List<Tag> tags = ZerofiltreUtils.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        Article article0 = ZerofiltreUtils.createMockArticle(user, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        article0JPA.setAuthor(userJPA);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtils.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, 0, FindArticleRequest.Filter.POPULAR, "java");

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article1");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article0");

    }

    @Test
    void articlesOf_returnsSelectedTag_InDescendingOrder_WithConnectedUser() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        List<Tag> tags = ZerofiltreUtils.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        //Article0 has no author
        Article article0 = ZerofiltreUtils.createMockArticle(null, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtils.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FindArticleRequest.Filter.POPULAR, "java");

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(1);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article1");

    }

    @Test
    void articlesOf_returnsInDescendingOrder_ByDefault() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);
        LocalDateTime now = LocalDateTime.now();


        List<Tag> tags = ZerofiltreUtils.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());


        Article article0 = ZerofiltreUtils.createMockArticle(user, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(now);
        article0JPA.setAuthor(userJPA);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtils.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(now.plusMinutes(1));
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(now.plusMinutes(2));
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, 0, null, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(3);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");
        assertThat(articles.getContent().get(2).getTitle()).isEqualTo("article0");

    }

    @Test
    void articlesOf_returnsInDescendingOrder_ByDefault_WithConnectedUser() {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);
        LocalDateTime now = LocalDateTime.now();


        List<Tag> tags = ZerofiltreUtils.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        //Article 0 has no author
        Article article0 = ZerofiltreUtils.createMockArticle(null, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(now);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtils.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(now.plusMinutes(1));
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(now.plusMinutes(2));
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FindArticleRequest.Filter.POPULAR, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");

    }
}