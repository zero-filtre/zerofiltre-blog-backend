package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@DataJpaTest
@Import({ArticleDatabaseProvider.class, TagDatabaseProvider.class, UserDatabaseProvider.class, ReactionDatabaseProvider.class})
class InitArticleIT {

    public static final String TITLE = "Title";
    private InitArticle initArticle;

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
        initArticle = new InitArticle(articleProvider);
    }

    @Test
    @DisplayName("Must create article with all data")
    void mustSetStatusToPublished() {
        //ARRANGE
        LocalDateTime beforeCreation = LocalDateTime.now();
        User mockUser = userProvider.create(ZerofiltreUtils.createMockUser());

        //ACT
        Article initializedArticle = initArticle.execute(TITLE, mockUser);

        //ASSERT
        assertThat(initializedArticle).isNotNull();
        assertThat(initializedArticle.getId()).isNotZero();

        assertThat(initializedArticle.getCreatedAt()).isNotNull();
        assertThat(initializedArticle.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(initializedArticle.getLastSavedAt()).isNotNull();
        assertThat(initializedArticle.getCreatedAt()).isBeforeOrEqualTo(initializedArticle.getLastSavedAt());
        assertThat(initializedArticle.getLastSavedAt()).isAfterOrEqualTo(beforeCreation);


        User publisher = initializedArticle.getAuthor();
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

        assertThat(initializedArticle.getTitle()).isEqualTo(TITLE);

        assertThat(initializedArticle.getStatus()).isEqualTo(DRAFT);

    }

}