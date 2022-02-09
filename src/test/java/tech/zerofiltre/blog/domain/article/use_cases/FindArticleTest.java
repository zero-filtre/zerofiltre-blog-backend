package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
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

    @BeforeEach
    void setUp() {
        findArticle = new FindArticle(articleProvider);
    }

    @Test
    @DisplayName("Must return the article corresponding ot the id")
    void mustReturnAnArticle() throws ResourceNotFoundException {
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
    void mustAllow_Asking_PublishedArticles_forAll() throws ForbiddenActionException {
        //ARRANGE
        Article published = new Article();
        published.setStatus(PUBLISHED);


        when(articleProvider.articlesOf(anyInt(), anyInt(), eq(PUBLISHED))).thenReturn(Collections.singletonList(published));

        //ACT
        List<Article> articles = findArticle.of(new FindArticleRequest(0, 3, PUBLISHED, new User()));
        List<Article> otherArticles = findArticle.of(new FindArticleRequest(0, 3, PUBLISHED, null));

        //ASSERT
        verify(articleProvider, times(2)).articlesOf(0, 3, PUBLISHED);
        assertThat(articles).isNotNull();
        assertThat(otherArticles).isNotNull();
        articles.forEach(article -> assertThat(PUBLISHED).isEqualTo(article.getStatus()));
        otherArticles.forEach(article -> assertThat(PUBLISHED).isEqualTo(article.getStatus()));
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
    @DisplayName("Not being an admin, ask for IN_REVIEW throws exception")
    void mustThrowException_IfNotAdminButAskForInReview() {
        //ARRANGE

        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class).
                isThrownBy(() -> findArticle.of(new FindArticleRequest(0, 3, IN_REVIEW, new User())));

    }
}