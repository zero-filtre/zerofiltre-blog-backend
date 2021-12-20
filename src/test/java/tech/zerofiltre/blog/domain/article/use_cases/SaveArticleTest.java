package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@ExtendWith(SpringExtension.class)
class SaveArticleTest {

    private SaveArticle saveArticle;

    @MockBean
    private ArticleProvider articleProvider;
    @MockBean
    private TagProvider tagProvider;
    @MockBean
    private UserProvider userProvider;

    @BeforeEach
    void init() {
        saveArticle = new SaveArticle(articleProvider, userProvider, tagProvider);
    }

    @Test
    @DisplayName("Must set the status to draft then create the article")
    void mustSetStatusToPublished() throws SaveArticleException {
        //ARRANGE
        LocalDateTime beforePublication = LocalDateTime.now();
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.setId(45);
        when(articleProvider.save(any())).thenReturn(mockArticle);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));

        //ACT
        Article publishedArticle = saveArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(publishedArticle.getLastSavedAt());
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);

        assertThat(publishedArticle.getAuthor()).isEqualTo(mockArticle.getAuthor());
        assertThat(publishedArticle.getContent()).isEqualTo(mockArticle.getContent());
        assertThat(publishedArticle.getThumbnail()).isEqualTo(mockArticle.getThumbnail());
        assertThat(publishedArticle.getTitle()).isEqualTo(mockArticle.getTitle());
        assertThat(publishedArticle.getTags()).hasSameElementsAs(mockArticle.getTags());
        assertThat(publishedArticle.getReactions()).hasSameElementsAs(mockArticle.getReactions());
        assertThat(publishedArticle.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    @DisplayName("Must register article if it is not yet registered")
    void mustRegisterWhenPublishing() throws SaveArticleException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));

        //ACT
        Article publishedArticle = saveArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();
    }

    @Test
    @DisplayName("Must throw SaveArticleException if tags are not saved")
    void mustThrowExceptionOnNoTags() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(SaveArticleException.class).isThrownBy(() -> saveArticle.execute(mockArticle));
    }

    @Test
    @DisplayName("Must throw SaveArticleException if author is not found")
    void mustThrowExceptionOnNoAuthor() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));

        //ACT & ASSERT
        assertThatExceptionOfType(SaveArticleException.class).isThrownBy(() -> saveArticle.execute(mockArticle));

    }

}