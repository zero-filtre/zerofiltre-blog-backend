package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@DataJpaTest
@Import({DBArticleProvider.class, DBTagProvider.class, DBUserProvider.class, DBReactionProvider.class})
class PublishOrSaveArticleIT {

    public static final String NEW_CONTENT = "New content";
    public static final String NEW_THUMBNAIL = "New thumbnail";
    public static final String NEW_TITLE = "New title";
    public static final String NEW_SUMMARY = "New summary";
    private PublishOrSaveArticle publishOrSaveArticle;

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private TagProvider tagProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ReactionProvider reactionProvider;

    @BeforeEach
    void init() {
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider);
    }

    @Test
    @DisplayName("Must properly partially save the data on publish")
    void mustPublishProperly() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE
        User user = userProvider.save(ZerofiltreUtils.createMockUser(true));

        List<Tag> newTags = ZerofiltreUtils.createMockTags(false).stream()
                .map(tagProvider::save)
                .collect(Collectors.toList());


        Article article = ZerofiltreUtils.createMockArticle(user, new ArrayList<>(), new ArrayList<>());
        article = articleProvider.save(article);

        ZerofiltreUtils.createMockReactions(true, article.getId(), 0,user)
                .forEach(reactionProvider::save);


        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        Article publishedArticle = publishOrSaveArticle.execute(user, article.getId(), NEW_TITLE, NEW_THUMBNAIL, NEW_SUMMARY, NEW_CONTENT, newTags, PUBLISHED);

        //ASSERT
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getSummary()).isNotNull();
        assertThat(publishedArticle.getSummary()).isEqualTo(NEW_SUMMARY);

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(publishedArticle.getPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getPublishedAt()).isBeforeOrEqualTo(publishedArticle.getLastPublishedAt());
        assertThat(publishedArticle.getLastPublishedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);


        User publisher = publishedArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(user.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(user.getId());
        assertThat(publisher.getFullName()).isEqualTo(user.getFullName());
        assertThat(publisher.getProfilePicture()).isEqualTo(user.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(publisher.getBio()).isEqualTo(user.getBio());
        assertThat(publisher.getProfession()).isEqualTo(user.getProfession());
        assertThat(publisher.getWebsite()).isEqualTo(user.getWebsite());

        Set<SocialLink> publishedSocialLinks = publisher.getSocialLinks();
        Set<SocialLink> userSocialLinks = user.getSocialLinks();
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

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must properly partially save the data on save")
    void mustSaveProperly() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE
        User user = userProvider.save(ZerofiltreUtils.createMockUser(false));

        List<Tag> newTags = ZerofiltreUtils.createMockTags(false).stream()
                .map(tagProvider::save)
                .collect(Collectors.toList());

        Article article = ZerofiltreUtils.createMockArticle(user, new ArrayList<>(), new ArrayList<>());
        article = articleProvider.save(article);

        ZerofiltreUtils.createMockReactions(true, article.getId(),0, user)
                .forEach(reactionProvider::save);


        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        Article savedArticle = publishOrSaveArticle.execute(user, article.getId(), NEW_TITLE, NEW_THUMBNAIL, NEW_SUMMARY, NEW_CONTENT, newTags, DRAFT);

        //ASSERT
        assertThat(savedArticle).isNotNull();
        assertThat(savedArticle.getId()).isNotZero();

        assertThat(savedArticle.getSummary()).isEqualTo(NEW_SUMMARY);


        assertThat(savedArticle.getCreatedAt()).isNotNull();
        assertThat(savedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(savedArticle.getLastSavedAt()).isNotNull();
        assertThat(savedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);


        User publisher = savedArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(user.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(user.getId());
        assertThat(publisher.getFullName()).isEqualTo(user.getFullName());
        assertThat(publisher.getProfilePicture()).isEqualTo(user.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(user.getPseudoName());

        assertThat(savedArticle.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(savedArticle.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
        assertThat(savedArticle.getTitle()).isEqualTo(NEW_TITLE);

        List<Tag> publishedArticleTags = savedArticle.getTags();

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(savedArticle.getStatus()).isEqualTo(DRAFT);
    }


}