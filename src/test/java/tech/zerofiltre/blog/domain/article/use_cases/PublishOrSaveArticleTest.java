package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
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
class PublishOrSaveArticleTest {

    public static final String NEW_CONTENT = "New content";
    public static final String NEW_THUMBNAIL = "New thumbnail";
    public static final String NEW_TITLE = "New title";
    public static final String NEW_SUMMARY = "New summary";
    Tag newTag = new Tag(12, "c++");


    private PublishOrSaveArticle publishOrSaveArticle;


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
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider);
    }

    @Test
    @DisplayName("Must properly partially update the data on publish")
    void mustPublishProperly() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE

        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.getAuthor().setRoles(Collections.singleton("ROLE_ADMIN"));
        mockArticle.setId(45);
        when(articleProvider.articleOfId(45)).thenReturn(Optional.of(mockArticle));

        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(tagProvider.tagOfId(12)).thenReturn(Optional.of(newTag));

        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        List<Tag> newTags = Collections.singletonList(newTag);
        Article publishedArticle = publishOrSaveArticle.execute(
                mockArticle.getAuthor(),
                45,
                NEW_TITLE,
                NEW_THUMBNAIL,
                NEW_SUMMARY,
                NEW_CONTENT,
                newTags,
                PUBLISHED
        );

        //ASSERT
        verify(articleProvider, times(1)).save(any());
        verify(articleProvider, times(1)).articleOfId(45);
        verify(tagProvider, times(1)).tagOfId(12);

        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
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


        assertThat(publishedArticle.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(publishedArticle.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
        assertThat(publishedArticle.getTitle()).isEqualTo(NEW_TITLE);


        List<Tag> publishedArticleTags = publishedArticle.getTags();
        List<Reaction> publishedArticleReactions = publishedArticle.getReactions();
        List<Reaction> articleReactions = mockArticle.getReactions();

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());
        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(publishedArticleReactions.stream().anyMatch(reaction ->
                articleReactions.stream().anyMatch(mockReaction ->
                        reaction.getId() == mockReaction.getId()
                                && reaction.getAction().equals(mockReaction.getAction())
                                && reaction.getAuthor().getFirstName().equals(mockReaction.getAuthor().getFirstName())
                                && reaction.getAuthor().getLastName().equals(mockReaction.getAuthor().getLastName())
                                && reaction.getAuthor().getProfilePicture().equals(mockReaction.getAuthor().getProfilePicture())
                                && reaction.getAuthor().getPseudoName().equals(mockReaction.getAuthor().getPseudoName())
                                && reaction.getAuthor().getRegisteredOn().equals(mockReaction.getAuthor().getRegisteredOn())
                                && reaction.getAuthor().getId() == mockReaction.getAuthor().getId())
        )).isTrue();

        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must not draft an already published article")
    void save_MustNotDraft_AnAlreadyPublishArticle() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.getAuthor().setRoles(Collections.singleton("ROLE_ADMIN"));
        mockArticle.setId(45);
        mockArticle.setStatus(PUBLISHED);
        when(articleProvider.articleOfId(45)).thenReturn(Optional.of(mockArticle));

        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(tagProvider.tagOfId(12)).thenReturn(Optional.of(newTag));

        //ACT
        List<Tag> newTags = Collections.singletonList(newTag);
        Article publishedArticle = publishOrSaveArticle.execute(
                mockArticle.getAuthor(),
                45,
                NEW_TITLE,
                NEW_THUMBNAIL,
                NEW_SUMMARY,
                NEW_CONTENT,
                newTags,
                DRAFT
        );

        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);

    }

    @Test
    @DisplayName("Must properly partially update the data on save")
    void mustSaveProperly() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE

        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.getAuthor().setRoles(Collections.singleton("ROLE_ADMIN"));
        mockArticle.setId(45);
        when(articleProvider.articleOfId(45)).thenReturn(Optional.of(mockArticle));

        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(tagProvider.tagOfId(12)).thenReturn(Optional.of(newTag));

        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        List<Tag> newTags = Collections.singletonList(newTag);
        Article publishedArticle = publishOrSaveArticle.execute(
                mockArticle.getAuthor(),
                45,
                NEW_TITLE,
                NEW_THUMBNAIL,
                NEW_SUMMARY,
                NEW_CONTENT,
                newTags,
                DRAFT
        );

        //ASSERT
        verify(articleProvider, times(1)).save(any());
        verify(articleProvider, times(1)).articleOfId(45);
        verify(tagProvider, times(1)).tagOfId(12);

        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
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

        Set<SocialLink> publishedSocialLinks = publisher.getSocialLinks();
        Set<SocialLink> userSocialLinks = mockUser.getSocialLinks();
        assertThat(publishedSocialLinks).hasSameSizeAs(userSocialLinks);
        assertThat(publishedSocialLinks.stream().anyMatch(socialLink ->
                userSocialLinks.stream().anyMatch(userSocialLink ->
                        socialLink.getLink().equals(userSocialLink.getLink()) &&
                                socialLink.getPlatform().equals(userSocialLink.getPlatform())
                )
        )).isTrue();


        assertThat(publishedArticle.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(publishedArticle.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
        assertThat(publishedArticle.getTitle()).isEqualTo(NEW_TITLE);


        List<Tag> publishedArticleTags = publishedArticle.getTags();
        List<Reaction> publishedArticleReactions = publishedArticle.getReactions();
        List<Reaction> articleReactions = mockArticle.getReactions();

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(publishedArticleReactions.stream().anyMatch(reaction ->
                articleReactions.stream().anyMatch(mockReaction ->
                        reaction.getId() == mockReaction.getId()
                                && reaction.getAction().equals(mockReaction.getAction())
                                && reaction.getAuthor().getFirstName().equals(mockReaction.getAuthor().getFirstName())
                                && reaction.getAuthor().getLastName().equals(mockReaction.getAuthor().getLastName())
                                && reaction.getAuthor().getProfilePicture().equals(mockReaction.getAuthor().getProfilePicture())
                                && reaction.getAuthor().getPseudoName().equals(mockReaction.getAuthor().getPseudoName())
                                && reaction.getAuthor().getRegisteredOn().equals(mockReaction.getAuthor().getRegisteredOn())
                                && reaction.getAuthor().getId() == mockReaction.getAuthor().getId())
        )).isTrue();

        assertThat(publishedArticle.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    @DisplayName("Must throw PublishArticleException if article is not yet registered")
    void mustThrowExceptionOnUnsavedArticle() {
        //ARRANGE
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.empty());


        //ACT & ASSERT
        assertThatExceptionOfType(PublishOrSaveArticleException.class)
                .isThrownBy(() -> publishOrSaveArticle.execute(new User(), 1, "", "", "", "", new ArrayList<>(), PUBLISHED));

    }

    @Test
    @DisplayName("Must throw ForbiddenActionException if editor is neither the author nor an admin ")
    void mustThrowForbiddenActionExceptionOnNotAuthor() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        User editor = new User();
        editor.setEmail("email");
        editor.setRoles(Collections.singleton("ROLE_USER"));


        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> publishOrSaveArticle.execute(editor, 1, "", "", "", "", new ArrayList<>(), PUBLISHED));

    }

    @Test
    @DisplayName("Must throw PublishArticleException if tags are not saved")
    void mustThrowExceptionOnNoTags() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.empty());
        User editor = new User();
        editor.setEmail(mockArticle.getAuthor().getEmail());
        editor.setRoles(Collections.singleton("ROLE_USER"));

        //ACT & ASSERT
        assertThatExceptionOfType(PublishOrSaveArticleException.class)
                .isThrownBy(() -> publishOrSaveArticle.execute(editor, 1, "", "", "", "", Collections.singletonList(newTag), PUBLISHED));
    }

}