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
import tech.zerofiltre.blog.domain.user.UserProvider;
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
@Import({DBArticleProvider.class, DBTagProvider.class, DBUserProvider.class})
class DBArticleProviderIT {

    @Autowired
    TagProvider tagProvider;
    @Autowired
    private ArticleJPARepository articleJPARepository;
    @Autowired
    private UserJPARepository userJPARepository;
    @Autowired
    private ReactionArticleJPARepository reactionArticleJPARepository;
    private ArticleJPAMapper articleJPAMapper = Mappers.getMapper(ArticleJPAMapper.class);

    private UserJPAMapper userJPAMapper = Mappers.getMapper(UserJPAMapper.class);

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {
    }

    @Test
    void articlesOf_returnsMostReacted_InDescendingOrder() {
        //ARRANGE
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        Article article0 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setAuthor(userJPA);
        articleJPARepository.save(article0JPA);

        ReactionArticleJPA reaction0 = new ReactionArticleJPA();
        reaction0.setAction(Reaction.Action.FIRE);
        reaction0.setArticle(article0JPA);
        reaction0.setAuthor(userJPA);

        ReactionArticleJPA reaction1 = new ReactionArticleJPA();
        reaction1.setAction(Reaction.Action.CLAP);
        reaction1.setArticle(article0JPA);
        reaction1.setAuthor(userJPA);

        ReactionArticleJPA reaction2 = new ReactionArticleJPA();
        reaction2.setAction(Reaction.Action.LOVE);
        reaction2.setArticle(article0JPA);
        reaction2.setAuthor(userJPA);

        article0JPA.setReactions(new HashSet<>(Arrays.asList(reaction0, reaction1, reaction2)));

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        ReactionArticleJPA reaction3 = new ReactionArticleJPA();
        reaction3.setAction(Reaction.Action.CLAP);
        reaction3.setAuthor(userJPA);
        reaction3.setArticle(article1JPA);

        ReactionArticleJPA reaction4 = new ReactionArticleJPA();
        reaction4.setAction(Reaction.Action.LOVE);
        reaction4.setAuthor(userJPA);
        reaction4.setArticle(article1JPA);


        article1JPA.setReactions(new HashSet<>(Arrays.asList(reaction3, reaction4)));


        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, 0, FinderRequest.Filter.POPULAR, null);

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
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        //article0JPA has no author
        Article article0 = ZerofiltreUtilsTest.createMockArticle(null, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article0JPA);

        ReactionArticleJPA reaction0 = new ReactionArticleJPA();
        reaction0.setAction(Reaction.Action.FIRE);
        reaction0.setArticle(article0JPA);
        reaction0.setAuthor(userJPA);

        ReactionArticleJPA reaction1 = new ReactionArticleJPA();
        reaction1.setAction(Reaction.Action.CLAP);
        reaction1.setArticle(article0JPA);
        reaction1.setAuthor(userJPA);

        ReactionArticleJPA reaction2 = new ReactionArticleJPA();
        reaction2.setAction(Reaction.Action.LOVE);
        reaction2.setArticle(article0JPA);
        reaction2.setAuthor(userJPA);

        article0JPA.setReactions(new HashSet<>(Arrays.asList(reaction0, reaction1, reaction2)));

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        ReactionArticleJPA reaction3 = new ReactionArticleJPA();
        reaction3.setAction(Reaction.Action.CLAP);
        reaction3.setAuthor(userJPA);
        reaction3.setArticle(article1JPA);

        ReactionArticleJPA reaction4 = new ReactionArticleJPA();
        reaction4.setAction(Reaction.Action.LOVE);
        reaction4.setAuthor(userJPA);
        reaction4.setArticle(article1JPA);


        article1JPA.setReactions(new HashSet<>(Arrays.asList(reaction3, reaction4)));


        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FinderRequest.Filter.POPULAR, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article1");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article2");

    }


    @Test
    void articlesOf_returnsMostViewed_InDescendingOrder() {
        //ARRANGE
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        Article article0 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setAuthor(userJPA);
        article0JPA.setViewsCount(0);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setAuthor(userJPA);
        article1JPA.setViewsCount(2);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setViewsCount(3);
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, 0, FinderRequest.Filter.MOST_VIEWED, null);

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
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        //article0JPA has no author
        Article article0 = ZerofiltreUtilsTest.createMockArticle(null, Collections.emptyList(), Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        article0JPA.setViewsCount(5);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        article1JPA.setViewsCount(2);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setViewsCount(3);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FinderRequest.Filter.MOST_VIEWED, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");

    }
    @Test
    void articlesOf_reliesOnTheProperFilter() {
        //ARRANGE
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        //article1JPA has 2 reactions and 5 views
        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        article1JPA.setViewsCount(5);
        articleJPARepository.save(article1JPA);

        ReactionArticleJPA reaction1 = new ReactionArticleJPA();
        reaction1.setAction(Reaction.Action.CLAP);
        reaction1.setAuthor(userJPA);
        reaction1.setArticle(article1JPA);

        ReactionArticleJPA reaction2 = new ReactionArticleJPA();
        reaction2.setAction(Reaction.Action.LOVE);
        reaction2.setAuthor(userJPA);
        reaction2.setArticle(article1JPA);
        article1JPA.setReactions(new HashSet<>(Arrays.asList(reaction1, reaction2)));


        //article1JPA has 0 reactions and 8 views
        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        article2JPA.setViewsCount(8);
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FinderRequest.Filter.MOST_VIEWED, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");

    }

    @Test
    void articlesOf_returnsSelectedTag_InDescendingOrder() {
        //ARRANGE
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        List<Tag> tags = ZerofiltreUtilsTest.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        Article article0 = ZerofiltreUtilsTest.createMockArticle(user, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        article0JPA.setAuthor(userJPA);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, 0, FinderRequest.Filter.POPULAR, "java");

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article1");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article0");

    }

    @Test
    void articlesOf_returnsSelectedTag_InDescendingOrder_WithConnectedUser() {
        //ARRANGE
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);

        List<Tag> tags = ZerofiltreUtilsTest.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        //Article0 has no author
        Article article0 = ZerofiltreUtilsTest.createMockArticle(null, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(LocalDateTime.now());
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(LocalDateTime.now());
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FinderRequest.Filter.POPULAR, "java");

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(1);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article1");

    }

    @Test
    void articlesOf_returnsInDescendingOrder_ByDefault() {
        //ARRANGE
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);
        LocalDateTime now = LocalDateTime.now();


        List<Tag> tags = ZerofiltreUtilsTest.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());


        Article article0 = ZerofiltreUtilsTest.createMockArticle(user, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(now);
        article0JPA.setAuthor(userJPA);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(now.plusMinutes(1));
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
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
        User user = ZerofiltreUtilsTest.createMockUser(false);
        UserJPA userJPA = userJPAMapper.toJPA(user);
        userJPARepository.save(userJPA);
        LocalDateTime now = LocalDateTime.now();


        List<Tag> tags = ZerofiltreUtilsTest.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        //Article 0 has no author
        Article article0 = ZerofiltreUtilsTest.createMockArticle(null, tags, Collections.emptyList());
        article0.setTitle("article0");
        ArticleJPA article0JPA = articleJPAMapper.toJPA(article0);
        article0JPA.setStatus(Status.PUBLISHED);
        article0JPA.setPublishedAt(now);
        articleJPARepository.save(article0JPA);

        Article article1 = ZerofiltreUtilsTest.createMockArticle(user, tags, Collections.emptyList());
        article1.setTitle("article1");
        ArticleJPA article1JPA = articleJPAMapper.toJPA(article1);
        article1JPA.setStatus(Status.PUBLISHED);
        article1JPA.setPublishedAt(now.plusMinutes(1));
        article1JPA.setAuthor(userJPA);
        articleJPARepository.save(article1JPA);

        Article article2 = ZerofiltreUtilsTest.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        article2.setTitle("article2");
        ArticleJPA article2JPA = articleJPAMapper.toJPA(article2);
        article2JPA.setAuthor(userJPA);
        article2JPA.setStatus(Status.PUBLISHED);
        article2JPA.setPublishedAt(now.plusMinutes(2));
        articleJPARepository.save(article2JPA);

        //ACT
        Page<Article> articles = articleProvider.articlesOf(0, 3, Status.PUBLISHED, userJPA.getId(), FinderRequest.Filter.POPULAR, null);

        //ASSERT
        assertThat(articles).isNotNull();
        assertThat(articles.getContent().size()).isEqualTo(2);
        assertThat(articles.getContent().get(0).getTitle()).isEqualTo("article2");
        assertThat(articles.getContent().get(1).getTitle()).isEqualTo("article1");

    }

    @Test
    @DisplayName("Count published articles by dates and user works properly")
    void countPublishedArticlesByDatesAndUser_works_properly() {
        //ARRANGE
        // -- dates
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        LocalDateTime threeMonthsBack = LocalDateTime.now().minusMonths(3);
        List<LocalDateTime> listDates = ZerofiltreUtils.getBeginningAndEndOfMonthDates();

        // -- Users
        User userA = new User();
        userA = userProvider.save(userA);

        User userB = new User();
        userB = userProvider.save(userB);

        // -- Articles
        Article articleA1 = new Article();
        articleA1.setAuthor(userA);
        articleA1.setLastPublishedAt(threeMonthsBack);
        articleProvider.save(articleA1);

        Article articleA2 = new Article();
        articleA2.setAuthor(userA);
        articleA2.setLastPublishedAt(lastMonth);
        articleProvider.save(articleA2);

        Article articleA3 = new Article();
        articleA3.setAuthor(userA);
        articleA3.setLastPublishedAt(lastMonth);
        articleProvider.save(articleA3);

        Article articleB1 = new Article();
        articleB1.setAuthor(userB);
        articleB1.setLastPublishedAt(threeMonthsBack);
        articleProvider.save(articleB1);

        Article articleB2 = new Article();
        articleB2.setAuthor(userB);
        articleB2.setLastPublishedAt(lastMonth);
        articleProvider.save(articleB2);

        //ACT
        int articlesPublishedByUserA = articleProvider.countPublishedArticlesByDatesAndUser(listDates.get(0), listDates.get(1), userA.getId());
        int articlesPublishedByUserB = articleProvider.countPublishedArticlesByDatesAndUser(listDates.get(0), listDates.get(1), userB.getId());

        //ASSERT
        assertThat(articlesPublishedByUserA).isNotNull();
        assertThat(articlesPublishedByUserA).isEqualTo(2);
        assertThat(articlesPublishedByUserB).isNotNull();
        assertThat(articlesPublishedByUserB).isEqualTo(1);
    }

    @Test
    @DisplayName("When I'm looking for new articles from last month, I return the list")
    void shouldReturnList_whenSearchingNewArticlesFromLastMonth() {
        //ARRANGE
        // -- dates
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        LocalDateTime threeMonthsBack = LocalDateTime.now().minusMonths(3);

        // -- Users
        User userA = new User();
        userA = userProvider.save(userA);

        User userB = new User();
        userB = userProvider.save(userB);

        // -- Articles
        Article articleA1 = new Article();
        articleA1.setAuthor(userA);
        articleA1.setLastPublishedAt(threeMonthsBack);
        articleProvider.save(articleA1);

        Article articleA2 = new Article();
        articleA2.setAuthor(userA);
        articleA2.setLastPublishedAt(lastMonth);
        articleA2 = articleProvider.save(articleA2);

        Article articleA3 = new Article();
        articleA3.setAuthor(userA);
        articleA3.setLastPublishedAt(lastMonth);
        articleA3 = articleProvider.save(articleA3);

        Article articleB1 = new Article();
        articleB1.setAuthor(userB);
        articleB1.setLastPublishedAt(threeMonthsBack);
        articleProvider.save(articleB1);

        Article articleB2 = new Article();
        articleB2.setAuthor(userB);
        articleB2.setLastPublishedAt(lastMonth);
        articleB2 = articleProvider.save(articleB2);

        //ACT
        List<Article> articleList = articleProvider.newArticlesFromLastMonth();

        //ASSERT
        assertThat(articleList.size()).isEqualTo(3);

        assertThat(articleList.get(0).getId()).isEqualTo(articleA2.getId());
        assertThat(articleList.get(0).getLastPublishedAt()).isEqualTo(articleA2.getLastPublishedAt());

        assertThat(articleList.get(1).getId()).isEqualTo(articleA3.getId());
        assertThat(articleList.get(1).getLastPublishedAt()).isEqualTo(articleA3.getLastPublishedAt());

        assertThat(articleList.get(2).getId()).isEqualTo(articleB2.getId());
        assertThat(articleList.get(2).getLastPublishedAt()).isEqualTo(articleB2.getLastPublishedAt());
    }

}