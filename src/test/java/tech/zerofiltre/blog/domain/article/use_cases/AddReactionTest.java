package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
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
        addReaction = new AddReaction(articleProvider, new Found_Published_WithKnownAuthor_CourseProvider_Spy());
    }

    @Test
    void execute_mustAddReactionAndUserInfoProperly_onArticle() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser.setId(2);
        Article mockArticle = ZerofiltreUtils.createMockArticle(null, new ArrayList<>(), new ArrayList<>());
        mockArticle.setStatus(Status.PUBLISHED);
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
    void execute_mustAddReactionAndUserInfoProperly_onCourse() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser.setId(2);
        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setCourseId(45);
        reaction.setAction(CLAP);

        Found_Published_WithKnownAuthor_CourseProvider_Spy courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        addReaction = new AddReaction(articleProvider, courseProvider);

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isNotZero();
        reactions.forEach(aReaction -> {
            assertThat(aReaction.getAction()).isEqualTo(CLAP);
            assertThat(aReaction.getCourseId()).isEqualTo(45);
            assertThat(aReaction.getAuthorId()).isEqualTo(2);
        });
        assertThat(courseProvider.registerCourseCalled).isTrue();

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
    @DisplayName("A user can not react more than 50 times no the same course")
    void execute_mustThrowException_IfUserAlreadyHas50Reactions_onACourse() {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);

        Reaction reactionOfTooMany = new Reaction();
        reactionOfTooMany.setAuthorId(currentUser.getId());
        reactionOfTooMany.setCourseId(1);
        reactionOfTooMany.setAction(CLAP);

        addReaction = new AddReaction(articleProvider, new Found_Published_With49Reactions_CourseProvider_Spy());

        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> addReaction.execute(reactionOfTooMany))
                .withMessage("You can not react on a course more that 50 times");

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
    void execute_ThrowsExceptionIfCourseIsNotFound() {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, currentUser, Collections.emptyList(), Collections.emptyList());
        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setCourseId(course.getId());
        reaction.setAction(CLAP);

        addReaction = new AddReaction(articleProvider, new NotFoundCourseProviderSpy());

        //ACT && ASSERT
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> addReaction.execute(reaction))
                .withMessage("We couldn't find the course you are trying to react on");
    }

    @Test
    void canNotReactOnAnUnpublishedArticle() {
        //ARRANGE

        Article article = new Article();
        article.setId(4);
        article.setStatus(Status.DRAFT);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(article));

        Reaction reaction = new Reaction();
        reaction.setArticleId(article.getId());

        //ACT && ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> addReaction.execute(reaction));
    }

    @Test
    void canNotReactOnAnUnpublishedCourse() {
        //ARRANGE
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        Reaction reaction = new Reaction();
        reaction.setCourseId(course.getId());

        addReaction = new AddReaction(articleProvider, new Found_Draft_WithKnownAuthor_CourseProvider_Spy());

        //ACT && ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> addReaction.execute(reaction))
                .withMessage("You can not react on an unpublished course");
    }
}
