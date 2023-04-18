package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({DBUserProvider.class, DBArticleProvider.class, DBTagProvider.class, DBReactionProvider.class, DBVerificationTokenProvider.class, DBArticleProvider.class, Slf4jLoggerProvider.class})
class DeleteArticleIT {

    private DeleteArticle deleteArticle;

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ArticleProvider articleProvider;
    @Autowired
    private ReactionProvider reactionProvider;
    @Autowired
    private TagProvider tagProvider;
    @Autowired
    private LoggerProvider loggerProvider;


    @BeforeEach
    void init() {
        deleteArticle = new DeleteArticle(articleProvider, loggerProvider);
    }

    @Test
    @DisplayName("Deleting an article, deletes it from DB")
    void shouldDeleteProperly() throws ForbiddenActionException, ResourceNotFoundException {
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);

        List<Tag> tags = ZerofiltreUtils.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        Article article = ZerofiltreUtils.createMockArticle(currentUser, tags, Collections.emptyList());
        article = articleProvider.save(article);

        List<Reaction> reactions = ZerofiltreUtils.createMockReactions(false, article.getId(),0, currentUser);
        reactions.forEach(reactionProvider::save);

        deleteArticle.execute(currentUser, article.getId());

        Optional<Article> deletedArticle = articleProvider.articleOfId(article.getId());
        assertThat(deletedArticle).isEmpty();
    }
}