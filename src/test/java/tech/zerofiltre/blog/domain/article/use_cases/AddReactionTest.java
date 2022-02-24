package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.*;

@ExtendWith(SpringExtension.class)
class AddReactionTest {

    private AddReaction addReaction;

    @MockBean
    private ArticleProvider articleProvider;

    @BeforeEach
    void init() {
        addReaction = new AddReaction(articleProvider);
    }

    @Test
    void execute_mustAddReactionAndUserInfoProperly_onArticle() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser.setId(2);
        Article mockArticle = ZerofiltreUtils.createMockArticle(null, new ArrayList<>(), new ArrayList<>());
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setArticleId(12);
        reaction.setAction(CLAP);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isNotZero();
        reactions.forEach(aReaction -> {
            assertThat(aReaction.getAction()).isEqualTo(CLAP);
            assertThat(aReaction.getArticleId()).isEqualTo(12);
            assertThat(aReaction.getAuthorId()).isEqualTo(2);
        });
        verify(articleProvider, times(1)).save(any());

    }

    @Test
    @DisplayName("A user can not react more than 50 times no the same article")
    void execute_mustThrowException_IfUserAlreadyHas50Reactions() {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        List<Reaction> currentUserReactions = new ArrayList<>();

        for (int i = 0; i < 48; i++) {
            Reaction reaction = new Reaction();
            reaction.setAuthorId(currentUser.getId());
            reaction.setAction(CLAP);
            currentUserReactions.add(reaction);
        }
        Reaction fireFromCurrentUser = new Reaction();
        fireFromCurrentUser.setAuthorId(currentUser.getId());
        fireFromCurrentUser.setAction(FIRE);
        currentUserReactions.add(fireFromCurrentUser);

        Reaction fireFromAnotherUser = new Reaction();
        fireFromAnotherUser.setAuthorId(12);
        fireFromAnotherUser.setAction(FIRE);
        currentUserReactions.add(fireFromAnotherUser);

        Article mockArticle = ZerofiltreUtils.createMockArticle(null, new ArrayList<>(), currentUserReactions);

        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));

        Reaction reactionOfTooMany = new Reaction();
        reactionOfTooMany.setAuthorId(currentUser.getId());
        reactionOfTooMany.setArticleId(12);
        reactionOfTooMany.setAction(CLAP);

        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> addReaction.execute(reactionOfTooMany));

    }

    @Test
    void execute_ThrowsExceptionIfArticleIsNotFound() {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.empty());
        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setArticleId(12);
        reaction.setAction(CLAP);

        //ACT && ASSERT
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> addReaction.execute(reaction));
    }

    @Test
    void canNotReactOnAnUnpublishedArticle(){
        assertThat(false).isTrue();
    }
}
