package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBUserProvider.class, DBArticleProvider.class, Slf4jLoggerProvider.class,
        DBVerificationTokenProvider.class, DBReactionProvider.class, DBCourseProvider.class})
class DeleteUserIT {

    public static final String TOKEN = "tokEN";
    private DeleteUser deleteUser;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private VerificationTokenProvider tokenProvider;

    @Autowired
    private CourseProvider courseProvider;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);


    @Autowired
    LoggerProvider loggerProvider;

    @Autowired
    private ReactionProvider reactionProvider;

    @BeforeEach
    void init() {
        deleteUser = new DeleteUser(userProvider, articleProvider, tokenProvider, reactionProvider, courseProvider, loggerProvider);
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
    @DisplayName("Deleting a user that does not have articles deletes him with his token from the DB")
    void deleteUser_WithNoArticles() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        Article draftArticle = ZerofiltreUtils.createMockArticle(author, Collections.emptyList(), Collections.emptyList());
        draftArticle = articleProvider.save(draftArticle);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, courseProvider, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        User user = ZerofiltreUtils.createMockUser(false);
        user.setEmail("another");
        user.setPseudoName("another");
        user = userProvider.save(user);

        VerificationToken verificationToken = new VerificationToken(user, TOKEN, expiryDate);
        tokenProvider.save(verificationToken);


        Reaction reaction2 = new Reaction();
        reaction2.setAction(Reaction.Action.FIRE);
        reaction2.setCourseId(course.getId());
        reaction2.setArticleId(draftArticle.getId());
        reaction2.setAuthorId(user.getId());

        reaction2 = reactionProvider.save(reaction2);

        //ACT
        deleteUser.execute(user, user.getId());

        Optional<User> deletedUser = userProvider.userOfId(user.getId());
        assertThat(deletedUser).isEmpty();

        Optional<VerificationToken> deletedToken = tokenProvider.ofToken(TOKEN);
        assertThat(deletedToken).isEmpty();

        Optional<Reaction> deletedReaction2 = reactionProvider.reactionOfId(reaction2.getId());
        assertThat(deletedReaction2).isEmpty();
    }
}
