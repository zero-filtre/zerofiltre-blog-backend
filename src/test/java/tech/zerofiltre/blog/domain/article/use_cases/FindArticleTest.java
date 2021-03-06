package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@ExtendWith(SpringExtension.class)
class FindArticleTest {

    @MockBean
    private ArticleProvider articleProvider;

    FindArticle findArticle;
    private int numberOfElements = 1;
    private int totalNumberOfPages = 4;

    @BeforeEach
    void setUp() {
        findArticle = new FindArticle(articleProvider);
    }


    @Test
    @DisplayName("Must return the article corresponding ot the id")
    void mustNotFilterOnAuthors() throws ResourceNotFoundException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));

        //ACT
        Article article = findArticle.byId(12);

        //ASSERT
        assertThat(article).isEqualTo(mockArticle);

    }

    @Test
    @DisplayName("Throw FindArticleException if no article is found")
    void mustThrowExceptionOnNoArticle() {
        //ARRANGE
        when(articleProvider.articleOfId(anyLong())).thenReturn(java.util.Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> findArticle.byId(12));


    }

    @Test
    @DisplayName("Can ask for PUBLISHED article whatever the user")
    void mustAllow_Asking_PublishedArticles_forAll() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        Article published = new Article();
        published.setStatus(PUBLISHED);


        when(articleProvider.articlesOf(anyInt(), anyInt(), eq(PUBLISHED), anyLong(), anyBoolean(), any())).thenReturn(
                new Page<>(1, 0, numberOfElements, 1, totalNumberOfPages, Collections.singletonList(published), true, false)
        );

        //ACT
        Page<Article> articles = findArticle.of(new FindArticleRequest(0, 3, PUBLISHED, new User()));

        //ASSERT
        verify(articleProvider, times(1)).articlesOf(0, 3, PUBLISHED, 0, false, null);
        assertThat(articles).isNotNull();
        articles.getContent().forEach(article -> assertThat(PUBLISHED).isEqualTo(article.getStatus()));
    }

    @Test
    @DisplayName("Not being an admin, ask for DRAFT throws exception")
    void mustThrowException_IfNotAdminButAskForDRAFT() {
        //ARRANGE

        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class).
                isThrownBy(() -> findArticle.of(new FindArticleRequest(0, 3, DRAFT, new User())));

    }

    @Test
    @DisplayName("Not being authenticated, ask for DRAFT throws unauthenticatedExecption")
    void mustThrowException_IfNotAuthenticatedButAskForDRAFT() {
        //ARRANGE

        //ACT & ASSERT
        assertThatExceptionOfType(UnAuthenticatedActionException.class).
                isThrownBy(() -> findArticle.of(new FindArticleRequest(0, 3, DRAFT, null)));

    }

    @Test
    @DisplayName("Must not filter on authors it the user is not requesting his own articles")
    void mustReturnAnArticle() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        Article published = new Article();
        when(articleProvider.articlesOf(anyInt(), anyInt(), eq(PUBLISHED), anyLong(), anyBoolean(), any())).thenReturn(
                new Page<>(1, 0, numberOfElements, 1, totalNumberOfPages, Collections.singletonList(published), true, false)
        );

        User user = new User();
        user.setId(24);

        //ACT
        findArticle.of(new FindArticleRequest(0, 3, PUBLISHED, user));

        //ASSERT
        //call with authorId = 0
        verify(articleProvider, times(1)).articlesOf(0, 3, PUBLISHED, 0, false, null);


    }

    @Test
    @DisplayName("Not being an admin, a user can ask for his OTHER THAN PUBLISHED articles")
    void mustAllow_Asking_YourDraftedArticles() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        Article drafted = new Article();
        drafted.setStatus(DRAFT);

        when(articleProvider.articlesOf(anyInt(), anyInt(), eq(DRAFT), anyLong(), anyBoolean(), any())).thenReturn(
                new Page<>(1, 0, numberOfElements, 1, totalNumberOfPages, Collections.singletonList(drafted), true, false)
        );

        //ACT
        FindArticleRequest request = new FindArticleRequest(0, 3, DRAFT, new User());
        request.setYours(true);
        Page<Article> articles = findArticle.of(request);

        //ASSERT
        verify(articleProvider, times(1)).articlesOf(0, 3, DRAFT, 0, false, null);
        assertThat(articles).isNotNull();
        articles.getContent().forEach(article -> assertThat(DRAFT).isEqualTo(article.getStatus()));
    }

    @Test
    @DisplayName("Not being an admin, ask for IN_REVIEW throws exception")
    void mustThrowException_IfNotAdminButAskForInReview() {
        //ARRANGE

        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class).
                isThrownBy(() -> findArticle.of(new FindArticleRequest(0, 3, IN_REVIEW, new User())));

    }

    @Test
    void mustCallArticleProvider_WithCorrectParams() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE


        FindArticleRequest request = new FindArticleRequest(0, 3, PUBLISHED, new User());
        request.setByPopularity(true);
        request.setTag("tag");

        findArticle.of(request);

        //ASSERT
        verify(articleProvider, times(1))
                .articlesOf(0, 3, PUBLISHED, 0, true, "tag");

    }

    @Test
    void mustCallArticleProvider_WithCorrectParamsAndUser() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        User user = new User();
        user.setId(2);
        FindArticleRequest request = new FindArticleRequest(0, 3, PUBLISHED, user);
        request.setYours(true);

        findArticle.of(request);

        //ASSERT
        verify(articleProvider, times(1))
                .articlesOf(0, 3, PUBLISHED, 2, false, null);

    }
}