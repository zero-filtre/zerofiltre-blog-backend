package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
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
@Import({ArticleDatabaseProvider.class, TagDatabaseProvider.class, UserDatabaseProvider.class})
class SaveArticleIT {

    private SaveArticle saveArticle;

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private TagProvider tagProvider;

    @Autowired
    private UserProvider userProvider;

    @BeforeEach
    void init() {
        saveArticle = new SaveArticle(articleProvider, userProvider, tagProvider);
    }

    @Test
    @DisplayName("Must create article with all data")
    void mustSetStatusToPublished() throws SaveArticleException {
        //ARRANGE
        LocalDateTime beforePublication = LocalDateTime.now();

        User mockUser = userProvider.create(ZerofiltreUtils.createMockUser());
        List<Tag> tags = ZerofiltreUtils.createMockTags(false).stream()
                .map(tag -> tagProvider.create(tag))
                .collect(Collectors.toList());

        Article mockArticle = ZerofiltreUtils.createMockArticle(mockUser, tags);


        //ACT
        Article publishedArticle = saveArticle.execute(mockArticle);

        //ASSERT
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(publishedArticle.getLastSavedAt());
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);


        User publisher = publishedArticle.getAuthor();
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
        assertThat(publishedArticle.getStatus()).isEqualTo(DRAFT);

    }

}