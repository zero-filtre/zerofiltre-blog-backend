package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
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
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.PublishOrSaveArticleVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.PasswordEncoderConfiguration;
import tech.zerofiltre.blog.infra.providers.api.config.APIClientConfiguration;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.api.so.StackOverflowLoginProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleViewProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBVerificationTokenProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.infra.security.config.DBUserDetailsService;
import tech.zerofiltre.blog.infra.security.config.LoginFirstAuthenticationEntryPoint;
import tech.zerofiltre.blog.infra.security.config.RoleRequiredAccessDeniedHandler;
import tech.zerofiltre.blog.infra.security.model.GithubAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.JwtAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.StackOverflowAuthenticationTokenProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class, Slf4jLoggerProvider.class, DBArticleViewProvider.class})
class ArticleControllerIT {

    public static final String TITLE = "Des applications très évolutives alignées aux derniers standards.";
    @MockBean
    ArticleProvider articleProvider;

    @MockBean
    UserProvider userProvider;

    @MockBean
    TagProvider tagProvider;

    @MockBean
    ReactionProvider reactionProvider;


    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;

    @MockBean
    MetricsProvider metricsProvider;

    @MockBean
    StackOverflowLoginProvider loginProvider;

    @MockBean
    GithubLoginProvider githubLoginProvider;

    @MockBean
    DBVerificationTokenProvider verificationTokenProvider;

    @MockBean
    ArticleViewProvider articleViewProvider;

    @MockBean
    UserNotificationProvider userNotificationProvider;


    Article mockArticle = ZerofiltreUtils.createMockArticle(true);
    PublishOrSaveArticleVM publishOrSaveArticleVM = new PublishOrSaveArticleVM(
            mockArticle.getId(),
            mockArticle.getTitle(),
            mockArticle.getThumbnail(),
            mockArticle.getSummary(),
            mockArticle.getContent(),
            mockArticle.getTags()
    );

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void init() {
        //ARRANGE
        mockArticle.setStatus(Status.PUBLISHED);
        doNothing().when(metricsProvider).incrementCounter(any());
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));
        when(articleProvider.save(any())).thenReturn(mockArticle);
        when(articleProvider.articlesOf(anyInt(), anyInt(), any(), anyLong(), any(), anyString())).thenReturn(
                new Page<>(1, 0, 1, 1, 4, Collections.singletonList(mockArticle), true, false)
        );
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle));
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle.getReactions().get(0)));

    }


    @Test
    @WithMockUser
    void onArticleInit_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/article")
                .param("title", TITLE);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));
        verify(userProvider, times(1)).userOfEmail(any());

    }

    @Test
    @WithMockUser
    void onArticlePublish_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/article/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveArticleVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));

    }

    @Test
    @WithMockUser
    void onArticlePublish_whenInValidInput_thenReturn400() throws Exception {
        //ARRANGE
        publishOrSaveArticleVM.setSummary("");


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/article/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveArticleVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());

    }

    @Test
    @WithMockUser
    void onArticlePublish_whenLessThan20Chars_thenReturn400() throws Exception {
        //ARRANGE
        publishOrSaveArticleVM.setSummary("less than 20 chars");


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/article/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveArticleVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());

    }

    @Test
    @WithMockUser
    void onArticlePublish_whenMoreThan20Chars_thenReturn400() throws Exception {
        //ARRANGE
        publishOrSaveArticleVM.setSummary(
                "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars" +
                        "less than 20 chars");


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/article/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveArticleVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());

    }

    @Test
    @WithMockUser
    void onArticleSave_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/article")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveArticleVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));

    }

    @Test
    @WithMockUser
    void onDeleteArticle_whenValidInput_thenReturn204() throws Exception {

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.delete("/article/12");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }


    @Test
    void onArticleById_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/article/12");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));

    }

    @Test
    void onArticleCards_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/article")
                .param("pageNumber", "2")
                .param("pageSize", "3")
                .param("status", "PUBLISHED")
                .param("filter", "most_viewed")
                .param("tag", "");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value(TITLE));

    }

    public String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapperBuilder.build().writeValueAsString(obj);
    }


}