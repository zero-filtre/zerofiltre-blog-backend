package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.*;

@DataJpaTest
@Import({DBArticleProvider.class, DBUserProvider.class, DBReactionProvider.class})
class AddReactionIT {

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ReactionProvider reactionProvider;

    AddReaction addReaction;

    @BeforeEach
    void init() {
        addReaction = new AddReaction(articleProvider);
    }

    @Test
    void execute_mustSaveReactionsProperly() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);
        long currentUserId = currentUser.getId();
        Article article = ZerofiltreUtils.createMockArticle(currentUser, new ArrayList<>(), new ArrayList<>());
        article.setStatus(Status.PUBLISHED);
        article = articleProvider.save(article);

        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        long articleId = article.getId();
        reaction.setArticleId(articleId);
        reaction.setAction(CLAP);

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isNotZero();
        reactions.forEach(aReaction -> {
            assertThat(aReaction.getAction()).isEqualTo(CLAP);
            assertThat(aReaction.getArticleId()).isEqualTo(articleId);
            assertThat(aReaction.getAuthorId()).isEqualTo(currentUserId);
        });

    }

    @Test
    void execute_returnsAllArticleReactions() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);
        long currentUserId = currentUser.getId();


        Article article = ZerofiltreUtils.createMockArticle(currentUser, new ArrayList<>(), new ArrayList<>());
        article = articleProvider.save(article);
        article.setStatus(Status.PUBLISHED);
        long articleId = article.getId();

        Reaction previousReaction = new Reaction();
        previousReaction.setAuthorId(currentUserId);
        previousReaction.setAction(Reaction.Action.FIRE);
        previousReaction.setArticleId(articleId);
        reactionProvider.save(previousReaction);

        article.getReactions().add(previousReaction);
        articleProvider.save(article);

        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setArticleId(articleId);
        reaction.setAction(CLAP);

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isEqualTo(2);


        assertThat(reactions.stream().allMatch(aReaction ->
                aReaction.getAction().equals(CLAP) || aReaction.getAction().equals(FIRE)
        )).isTrue();

        assertThat(reactions.stream().allMatch(aReaction ->
                aReaction.getArticleId() == articleId &&
                        aReaction.getAuthorId() == currentUserId
        )).isTrue();

    }
}
