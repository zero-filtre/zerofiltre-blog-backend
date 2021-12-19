package tech.zerofiltre.blog.domain.article.useCases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@ExtendWith(SpringExtension.class)
class PublishArticleTest {

    private PublishArticle publishArticle;

    @MockBean
    private ArticleProvider articleProvider;

    @BeforeEach
    void init() {
        publishArticle = new PublishArticle(articleProvider);
    }

    @Test
    @DisplayName("Must set the status to published then save the article")
    void mustSetStatusToPublished() {
        //ARRANGE
        LocalDateTime beforePublication = LocalDateTime.now();
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.setId(45);
        when(articleProvider.save(any())).thenReturn(mockArticle);

        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotNull();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(publishedArticle.getPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastPublishedAt()).isNotNull();
        assertThat(publishedArticle.getPublishedAt()).isBeforeOrEqualTo(publishedArticle.getLastPublishedAt());
        assertThat(publishedArticle.getLastPublishedAt()).isAfterOrEqualTo(beforePublication);

        assertThat(publishedArticle.getAuthor()).isEqualTo(mockArticle.getAuthor());
        assertThat(publishedArticle.getContent()).isEqualTo(mockArticle.getContent());
        assertThat(publishedArticle.getThumbnail()).isEqualTo(mockArticle.getThumbnail());
        assertThat(publishedArticle.getTitle()).isEqualTo(mockArticle.getTitle());
        assertThat(publishedArticle.getTags()).hasSameElementsAs(mockArticle.getTags());
        assertThat(publishedArticle.getReactions()).hasSameElementsAs(mockArticle.getReactions());
        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must register article if it is not yet register")
    void mustRegisterWhenPublishing() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });

        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotNull();
    }

    @Test
    @DisplayName("Must save tags")
    void mustSaveTags() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article article = invocationOnMock.getArgument(0);
            article.getTags().forEach(tag -> tag.setId(4));
            return article;
        });

        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getTags().size()).isEqualTo(3);
        publishedArticle.getTags().forEach(tag -> assertThat(tag.getId()).isNotNull());
    }

}