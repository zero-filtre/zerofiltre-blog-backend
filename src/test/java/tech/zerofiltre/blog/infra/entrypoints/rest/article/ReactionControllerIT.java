package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.PasswordEncoderConfiguration;
import tech.zerofiltre.blog.infra.providers.api.config.APIClientConfiguration;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.api.so.StackOverflowLoginProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.security.config.DBUserDetailsService;
import tech.zerofiltre.blog.infra.security.config.LoginFirstAuthenticationEntryPoint;
import tech.zerofiltre.blog.infra.security.config.RoleRequiredAccessDeniedHandler;
import tech.zerofiltre.blog.infra.security.model.GithubAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.JwtAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.StackOverflowAuthenticationTokenProperties;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReactionController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class,StackOverflowAuthenticationTokenProperties.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class, DBCourseProvider.class})
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
    CourseProvider courseProvider;

    @MockBean
    SecurityContextManager securityContextManager;

    @MockBean
    ArticleProvider articleProvider;

    @MockBean
    MetricsProvider metricsProvider;


    @Test
    @WithMockUser
    void addArticle_returns200_onValidInput() throws Exception {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        Article article = new Article();
        article.setStatus(Status.PUBLISHED);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(article));

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
