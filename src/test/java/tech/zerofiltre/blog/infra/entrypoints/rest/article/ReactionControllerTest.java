package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.features.AddReaction;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.CLAP;

@ExtendWith(SpringExtension.class)
class ReactionControllerTest {

    private ReactionController reactionController;

    @MockBean
    ArticleProvider articleProvider;

    @Mock
    CourseProvider courseProvider;

    @MockBean
    SecurityContextManager securityContextManager;

    @Mock
    AddReaction addReaction;

    @BeforeEach
    void setUp() {
        reactionController = new ReactionController(articleProvider, courseProvider, securityContextManager);
        ReflectionTestUtils.setField(reactionController, "addReaction", addReaction);
    }

    @Test
    void addReactionOnArticle_constructAProperReaction() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        doReturn(Collections.emptyList()).when(addReaction).execute(any());
        User currentUser = new User();
        currentUser.setId(10);
        when(securityContextManager.getAuthenticatedUser()).thenReturn(currentUser);


        //ACT
        reactionController.addReaction(12, 0, "CLAP");

        //ASSERT
        ArgumentCaptor<Reaction> captor = ArgumentCaptor.forClass(Reaction.class);
        verify(addReaction, times(1)).execute(captor.capture());
        Reaction reaction = captor.getValue();
        assertThat(reaction.getAuthorId()).isEqualTo(10);
        assertThat(reaction.getAction()).isEqualTo(CLAP);
        assertThat(reaction.getArticleId()).isEqualTo(12);
    }

    @Test
    void addReactionOnCourse_constructAProperReaction() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        doReturn(Collections.emptyList()).when(addReaction).execute(any());
        User currentUser = new User();
        currentUser.setId(10);
        when(securityContextManager.getAuthenticatedUser()).thenReturn(currentUser);


        //ACT
        reactionController.addReaction(0, 12, "CLAP");

        //ASSERT
        ArgumentCaptor<Reaction> captor = ArgumentCaptor.forClass(Reaction.class);
        verify(addReaction, times(1)).execute(captor.capture());
        Reaction reaction = captor.getValue();
        assertThat(reaction.getAuthorId()).isEqualTo(10);
        assertThat(reaction.getAction()).isEqualTo(CLAP);
        assertThat(reaction.getCourseId()).isEqualTo(12);
    }
}