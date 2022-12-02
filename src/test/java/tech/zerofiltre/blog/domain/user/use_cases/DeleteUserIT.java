package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBUserProvider.class, DBArticleProvider.class, Slf4jLoggerProvider.class, DBVerificationTokenProvider.class, DBReactionProvider.class})
class DeleteUserIT {

    public static final String TOKEN = "tokEN";
    private DeleteUser deleteUser;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private VerificationTokenProvider tokenProvider;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);


    @Autowired
    LoggerProvider loggerProvider;

    @Autowired
    private ReactionProvider reactionProvider;

    @BeforeEach
    void init() {
        deleteUser = new DeleteUser(userProvider, articleProvider, tokenProvider, reactionProvider, loggerProvider);
    }

    @Test
    @DisplayName("Deleting a user that has articles deactivates the user")
    void deleteUser_WithArticles_deactivatesHim() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);
        Article draftArticle = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        articleProvider.save(draftArticle);

        //ACT
        deleteUser.execute(user, user.getId());

        Optional<User> updatedUser = userProvider.userOfId(user.getId());
        assertThat(updatedUser).isNotEmpty();
        assertThat(updatedUser.get().isExpired()).isTrue();


    }

    @Test
    @DisplayName("Deleting a user that do not have articles deletes him with his token from the DB")
    void deleteUser_WithNoArticles() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User articleAuthor = ZerofiltreUtils.createMockUser(false);
        articleAuthor = userProvider.save(articleAuthor);

        Article draftArticle = ZerofiltreUtils.createMockArticle(articleAuthor, Collections.emptyList(), Collections.emptyList());
        draftArticle = articleProvider.save(draftArticle);

        User user = ZerofiltreUtils.createMockUser(false);
        user.setEmail("another");
        user.setPseudoName("another");
        user = userProvider.save(user);

        VerificationToken verificationToken = new VerificationToken(user, TOKEN,expiryDate);
        tokenProvider.save(verificationToken);

        Reaction reaction = new Reaction();
        reaction.setAction(Reaction.Action.FIRE);
        reaction.setArticleId(draftArticle.getId());
        reaction.setAuthorId(user.getId());

        reaction = reactionProvider.save(reaction);

        //ACT
        deleteUser.execute(user, user.getId());

        Optional<User> deletedUser = userProvider.userOfId(user.getId());
        assertThat(deletedUser).isEmpty();

        Optional<VerificationToken> deletedToken = tokenProvider.ofToken(TOKEN);
        assertThat(deletedToken).isEmpty();

        Optional<Reaction> deletedReaction = reactionProvider.reactionOfId(reaction.getId());
        assertThat(deletedReaction).isEmpty();
    }
}
