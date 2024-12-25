package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
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
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.AvatarProvider;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.features.*;
import tech.zerofiltre.blog.domain.user.model.JwtToken;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.PasswordEncoderConfiguration;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.RegisterUserVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.ResetPasswordVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.UpdateUserVM;
import tech.zerofiltre.blog.infra.providers.BasicPasswordVerifierProvider;
import tech.zerofiltre.blog.infra.providers.api.config.APIClientConfiguration;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.api.so.StackOverflowLoginProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleViewProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.infra.providers.notification.user.AppPublisherNotificationProvider;
import tech.zerofiltre.blog.infra.security.config.DBUserDetailsService;
import tech.zerofiltre.blog.infra.security.config.LoginFirstAuthenticationEntryPoint;
import tech.zerofiltre.blog.infra.security.config.RoleRequiredAccessDeniedHandler;
import tech.zerofiltre.blog.infra.security.model.GithubAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.JwtAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.StackOverflowAuthenticationTokenProperties;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, BasicPasswordVerifierProvider.class, StackOverflowAuthenticationTokenProperties.class,
        AppPublisherNotificationProvider.class, APIClientConfiguration.class, Slf4jLoggerProvider.class, GithubAuthenticationTokenProperties.class,
        DBTagProvider.class, DBChapterProvider.class, DBArticleViewProvider.class, DBCourseProvider.class, DBCompanyCourseProvider.class})
class UserControllerIT {

    public static final String EMAIL = "email@toto.fr";
    public static final String NEW_BIO = "NEW_BIO";
    private static final String TOKEN = "token";
    private static final String PASSWORD = "COmplic$t6d";
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserProvider userProvider;

    @MockBean
    StackOverflowLoginProvider loginProvider;

    @MockBean
    AvatarProvider avatarProvider;

    @MockBean
    ReactionProvider reactionProvider;

    @MockBean
    CourseProvider courseProvider;

    @MockBean
    ArticleViewProvider articleViewProvider;

    @MockBean
    GithubLoginProvider githubLoginProvider;

    @MockBean
    NotifyRegistrationComplete notifyRegistrationComplete;

    @MockBean
    ResendRegistrationConfirmation resendRegistrationConfirmation;

    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    @MockBean
    ConfirmUserRegistration confirmUserRegistration;

    @MockBean
    ArticleProvider articleProvider;

    @MockBean
    MetricsProvider metricsProvider;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    TagProvider tagProvider;

    @MockBean
    ChapterProvider chapterProvider;

    @MockBean
    CompanyProvider companyProvider;

    @MockBean
    CompanyCourseProvider companyCourseProvider;

    @MockBean
    DataChecker checker;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;


    RegisterUserVM registerUserVM;

    @BeforeEach
    void init() throws UserNotFoundException, InvalidTokenException {
        when(userProvider.userOfEmail(any())).thenReturn(Optional.empty());
        doNothing().when(metricsProvider).incrementCounter(any());
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(notifyRegistrationComplete).execute(any(), any(), any(), any());
        doNothing().when(resendRegistrationConfirmation).execute(any(), any(), any());
        when(confirmUserRegistration.execute(any())).thenReturn(new User());
        VerificationToken t = new VerificationToken(new User(), TOKEN, expiryDate);
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(t));
        when(verificationTokenProvider.generate(any())).thenReturn(t);
        when(verificationTokenProvider.generate(any(), anyLong())).thenReturn(t);
        JwtToken jwtToken = new JwtToken(TOKEN, 864252546);
        when(jwtTokenProvider.generate(any())).thenReturn(jwtToken);
    }


    @Test
    void onUserCreation_onValidInput_thenReturn200() throws Exception {
        registerUserVM = new RegisterUserVM(
                "firstName lastName",
                PASSWORD,
                PASSWORD,
                "hola@zerofiltre.fr"
        );


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(registerUserVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").exists())
                .andExpect(jsonPath("refreshToken").exists())
                .andExpect(jsonPath("accessTokenExpiryDateInSeconds").exists())
                .andExpect(jsonPath("refreshTokenExpiryDateInSeconds").exists())
                .andExpect(jsonPath("tokenType").exists());


    }

    @Test
    void onUserCreation_onInValidInput_thenReturn400() throws Exception {
        registerUserVM = new RegisterUserVM(
                "firstName lastName",
                "password",
                "password",
                "hola@zerofiltre"
        );

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(registerUserVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());


    }

    @Test
    @WithMockUser
    void onResendRegistrationConfirm_onValidInput_thenReturn200() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user/resendRegistrationConfirm")
                .param("email", EMAIL);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    void onRegistrationConfirm_onValidInput_thenReturn200() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user/registrationConfirm")
                .param("token", "token");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    void onUserCreation_onNoMatchingPasswords_thenReturn400() throws Exception {
        registerUserVM = new RegisterUserVM(
                "firstName lastName",
                "password",
                "pas$word",
                "info@zerofiltre.tech"
        );

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(registerUserVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));


    }

    @Test
    void onUserCreation_onInvalidEmail_thenReturn400() throws Exception {
        registerUserVM = new RegisterUserVM(
                "firstName lastName",
                "password",
                "password",
                "hola@zerofiltre"
        );

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(registerUserVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));


    }

    @Test
    void onUserCreation_onEmptyData_thenReturn400() throws Exception {

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));


    }

    @Test
    @WithMockUser
    void onInitPasswordReset_onValidInput_thenReturn200() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user/initPasswordReset")
                .param("email", EMAIL);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    void onVerifyTokenForPasswordReset_onValidData_thenReturn200_withToken() throws Exception {
        //ARRANGE

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user/verifyTokenForPasswordReset")
                .param("token", TOKEN);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(TOKEN));

    }

    @Test
    void onSavePasswordReset_onValidInput_thenReturn200() throws Exception {
        //ARRANGE
        ResetPasswordVM resetPasswordVM = new ResetPasswordVM(TOKEN, PASSWORD, PASSWORD);

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user/savePasswordReset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(resetPasswordVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    @WithMockUser
    void onDeleteUser_onValidInputWith_ThenReturn200() throws Exception {
        //ARRANGE
        User connectedUser = new User();
        connectedUser.setId(12);
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(connectedUser));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(connectedUser));


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.delete("/user/12");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    void onGetGithubToken_onValidInput_thenReturn200() throws Exception {
        //ARRANGE
        when(githubLoginProvider.tokenFromCode(any())).thenReturn(TOKEN);

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user/github/accessToken")
                .param("code", "code");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").exists())
                .andExpect(jsonPath("refreshToken").exists())
                .andExpect(jsonPath("accessTokenExpiryDateInSeconds").exists())
                .andExpect(jsonPath("refreshTokenExpiryDateInSeconds").exists())
                .andExpect(jsonPath("tokenType").exists());
    }

    @Test
    void onGetGithubToken_onNullToken_thenReturn404() throws Exception {
        //ARRANGE
        when(githubLoginProvider.tokenFromCode(any())).thenReturn(null);

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/user/github/accessToken")
                .param("code", "code");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @WithMockUser
    void onGetUser_thenReturn200() throws Exception {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser
    void onGetUser_onNoUser_returns404() throws Exception {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.empty());

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser
    void onUpdateUser_ifValidInput_thenReturn200() throws Exception {
        //ARRANGE
        UpdateUserVM updateUserVM = new UpdateUserVM();
        updateUserVM.setBio(NEW_BIO);
        updateUserVM.setId(1);
        updateUserVM.setLanguage("fr");
        updateUserVM.setFullName("first name last name");

        User user = new User();
        user.setBio(NEW_BIO);
        user.setId(1);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateUserVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("bio").value(NEW_BIO));
    }

    @Test
    @WithMockUser
    void onGetArticles_ifValidInput_return200() throws Exception {

        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        when(articleProvider.articlesOf(anyInt(), anyInt(), any(), anyLong(), any(), anyString())).thenReturn(
                new Page<>(1, 0, 1, 1, 4, Collections.singletonList(mockArticle), true, false));

        User user = new User();
        user.setBio(NEW_BIO);
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user/articles")
                .param("pageNumber", "2")
                .param("pageSize", "3")
                .param("status", "DRAFT")
                .param("filter", "POPULAR")
                .param("tag", "");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value("Des applications très évolutives alignées aux derniers standards."));
    }

    @Test
    void onRefreshToken_ifRefreshingTokenValid_return200() throws Exception {
        //ARRANGE

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user/jwt/refreshToken")
                .param("refreshToken", TOKEN);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").exists())
                .andExpect(jsonPath("refreshToken").exists())
                .andExpect(jsonPath("accessTokenExpiryDateInSeconds").exists())
                .andExpect(jsonPath("refreshTokenExpiryDateInSeconds").exists())
                .andExpect(jsonPath("tokenType").exists());

    }

    public String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapperBuilder.build().writeValueAsString(obj);
    }
}
