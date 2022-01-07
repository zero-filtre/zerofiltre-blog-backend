package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

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
    void mustReturnAnArticle() throws ArticleNotFoundException {
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
        assertThatExceptionOfType(ArticleNotFoundException.class).isThrownBy(() -> findArticle.byId(12));


    }

    @Test
    @DisplayName("Must call provider")
    void articlesOf() {
        //ARRANGE
        when(articleProvider.articlesOf(anyInt(), anyInt())).thenReturn(new ArrayList<>());

        //ACT
        List<Article> articles = findArticle.of(0, 3);

        //ASSERT
        verify(articleProvider, times(1)).articlesOf(0, 3);
        assertThat(articles).isNotNull();
    }
}