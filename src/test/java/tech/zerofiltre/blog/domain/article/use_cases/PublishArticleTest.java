package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@ExtendWith(SpringExtension.class)
class PublishArticleTest {

    private PublishArticle publishArticle;

    @MockBean
    private ArticleProvider articleProvider;
    @MockBean
    private TagProvider tagProvider;
    @MockBean
    private UserProvider userProvider;
    @MockBean
    private ReactionProvider reactionProvider;

    @BeforeEach
    void init() {
        publishArticle = new PublishArticle(articleProvider, userProvider, tagProvider, reactionProvider);
    }

    @Test
    @DisplayName("Must set the status to published then save the article")
    void mustSetStatusToPublished() throws PublishArticleException {
        //ARRANGE
        LocalDateTime beforePublication = LocalDateTime.now();

        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.setId(45);
        when(articleProvider.save(any())).thenReturn(mockArticle);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle.getReactions().get(0)));


        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getPublishedAt()).isBeforeOrEqualTo(publishedArticle.getLastPublishedAt());
        assertThat(publishedArticle.getLastPublishedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);

        User publisher = publishedArticle.getAuthor();
        User mockUser = mockArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(mockUser.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(mockUser.getId());
        assertThat(publisher.getFirstName()).isEqualTo(mockUser.getFirstName());
        assertThat(publisher.getLastName()).isEqualTo(mockUser.getLastName());
        assertThat(publisher.getProfilePicture()).isEqualTo(mockUser.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(mockUser.getPseudoName());


        assertThat(publishedArticle.getContent()).isEqualTo(mockArticle.getContent());
        assertThat(publishedArticle.getThumbnail()).isEqualTo(mockArticle.getThumbnail());
        assertThat(publishedArticle.getTitle()).isEqualTo(mockArticle.getTitle());


        List<Tag> publishedArticleTags = publishedArticle.getTags();
        List<Tag> articleTags = mockArticle.getTags();

        assertThat(publishedArticleTags.size()).isEqualTo(articleTags.size());
        for (int i = 0; i < publishedArticleTags.size(); i++) {
            assertThat(publishedArticleTags.get(i).getId()).isEqualTo(articleTags.get(i).getId());
            assertThat(publishedArticleTags.get(i).getName()).isEqualTo(articleTags.get(i).getName());
        }

        assertThat(publishedArticle.getReactions()).hasSameElementsAs(mockArticle.getReactions());
        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must publish properly event if dates are null")
    void mustWorkWithNullDates() throws PublishArticleException {
        //ARRANGE
        LocalDateTime beforePublication = LocalDateTime.now();

        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.setCreatedAt(null);
        mockArticle.setPublishedAt(null);
        mockArticle.setId(45);
        when(articleProvider.save(any())).thenReturn(mockArticle);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle.getReactions().get(0)));


        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getPublishedAt()).isBeforeOrEqualTo(publishedArticle.getLastPublishedAt());
        assertThat(publishedArticle.getLastPublishedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);

        User publisher = publishedArticle.getAuthor();
        User mockUser = mockArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(mockUser.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(mockUser.getId());
        assertThat(publisher.getFirstName()).isEqualTo(mockUser.getFirstName());
        assertThat(publisher.getLastName()).isEqualTo(mockUser.getLastName());
        assertThat(publisher.getProfilePicture()).isEqualTo(mockUser.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(mockUser.getPseudoName());


        assertThat(publishedArticle.getContent()).isEqualTo(mockArticle.getContent());
        assertThat(publishedArticle.getThumbnail()).isEqualTo(mockArticle.getThumbnail());
        assertThat(publishedArticle.getTitle()).isEqualTo(mockArticle.getTitle());


        List<Tag> publishedArticleTags = publishedArticle.getTags();
        List<Tag> articleTags = mockArticle.getTags();

        assertThat(publishedArticleTags.size()).isEqualTo(articleTags.size());
        for (int i = 0; i < publishedArticleTags.size(); i++) {
            assertThat(publishedArticleTags.get(i).getId()).isEqualTo(articleTags.get(i).getId());
            assertThat(publishedArticleTags.get(i).getName()).isEqualTo(articleTags.get(i).getName());
        }

        assertThat(publishedArticle.getReactions()).hasSameElementsAs(mockArticle.getReactions());
        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must register article if it is not yet registered")
    void mustRegisterWhenPublishing() throws PublishArticleException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle.getReactions().get(0)));

        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();
    }

    @Test
    @DisplayName("Must throw PublishArticleException if tags are not saved")
    void mustThrowExceptionOnNoTags() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle.getReactions().get(0)));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(PublishArticleException.class).isThrownBy(() -> publishArticle.execute(mockArticle));
    }

    @Test
    @DisplayName("Must throw PublishArticleException if author is not found")
    void mustThrowExceptionOnNoAuthor() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle.getReactions().get(0)));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));

        //ACT & ASSERT
        assertThatExceptionOfType(PublishArticleException.class).isThrownBy(() -> publishArticle.execute(mockArticle));

    }

    @Test
    @DisplayName("Must throw PublishArticleException if author is null")
    void mustThrowExceptionOnNullAuthor() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.setAuthor(null);

        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));



        //ACT & ASSERT
        assertThatExceptionOfType(PublishArticleException.class).isThrownBy(() -> publishArticle.execute(mockArticle));

    }

    @Test
    @DisplayName("Must throw PublishArticleException if the article has unsaved reactions")
    void mustThrowExceptionOnUnknownReactions() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(PublishArticleException.class).isThrownBy(() -> publishArticle.execute(mockArticle));

    }

}