package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.util.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.CLAP;

@ExtendWith(SpringExtension.class)
class ReactionControllerTest {

    private ReactionController reactionController;

    @MockBean
    ArticleProvider articleProvider;

    @MockBean
    SecurityContextManager securityContextManager;

    @Mock
    AddReaction addReaction;

    @BeforeEach
    void setUp() {
        reactionController = new ReactionController(articleProvider, securityContextManager);
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
        reactionController.addReaction(12,0, "CLAP");

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
        reactionController.addReaction(0,12, "CLAP");

        //ASSERT
        ArgumentCaptor<Reaction> captor = ArgumentCaptor.forClass(Reaction.class);
        verify(addReaction, times(1)).execute(captor.capture());
        Reaction reaction = captor.getValue();
        assertThat(reaction.getAuthorId()).isEqualTo(10);
        assertThat(reaction.getAction()).isEqualTo(CLAP);
        assertThat(reaction.getCourseId()).isEqualTo(12);
    }
}