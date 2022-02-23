package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.http.converter.json.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.security.config.*;
import tech.zerofiltre.blog.infra.security.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReactionController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class,StackOverflowAuthenticationTokenProperties.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class})
class ReactionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProvider userProvider;

    @MockBean
    StackOverflowLoginProvider stackOverflowLoginProvider;

    @MockBean
    private GithubLoginProvider githubLoginProvider;

    @MockBean
    private VerificationTokenProvider verificationTokenProvider;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    SecurityContextManager securityContextManager;

    @MockBean
    ArticleProvider articleProvider;


    @Test
    @WithMockUser
    void addArticle_returns201Created_onValidInput() throws Exception {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(new Article()));

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/reaction")
                .param("articleId", "12")
                .param("action", "Clap");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is(200))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void addArticle_returns401_onNotAuthenticatedUser() throws Exception {

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/reaction")
                .param("articleId", "12")
                .param("action", "Clap");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is(401));
    }
}
