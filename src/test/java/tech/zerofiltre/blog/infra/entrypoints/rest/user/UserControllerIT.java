package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import com.fasterxml.jackson.core.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.http.converter.json.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;
import tech.zerofiltre.blog.infra.providers.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.providers.notification.user.*;
import tech.zerofiltre.blog.infra.security.config.*;
import tech.zerofiltre.blog.infra.security.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, BasicPasswordVerifierProvider.class, StackOverflowAuthenticationTokenProperties.class,
        UserMailNotificationProvider.class, APIClientConfiguration.class, GithubAuthenticationTokenProperties.class})
class UserControllerIT {

    public static final String EMAIL = "email@toto.fr";
    private static final String TOKEN = "token";
    private static final String PASSWORD = "COmplic$t6d";
    public static final String NEW_BIO = "NEW_BIO";
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserProvider userProvider;

    @MockBean
    StackOverflowLoginProvider loginProvider;

    @MockBean
    AvatarProvider avatarProvider;

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
    JwtTokenProvider jwtTokenProvider;


    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;


    RegisterUserVM registerUserVM;

    @BeforeEach
    void init() throws UserNotFoundException, InvalidTokenException {
        when(userProvider.userOfEmail(any())).thenReturn(Optional.empty());
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(notifyRegistrationComplete).execute(any(), any(), any());
        doNothing().when(resendRegistrationConfirmation).execute(any(), any(), any());
        when(confirmUserRegistration.execute(any())).thenReturn(new User());
        VerificationToken t = new VerificationToken(new User(), TOKEN);
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(t));
        when(verificationTokenProvider.generate(any())).thenReturn(t);
        when(verificationTokenProvider.generate(any(), anyLong())).thenReturn(t);
        JwtToken jwtToken = new JwtToken(TOKEN, 864252546);
        when(jwtTokenProvider.generate(any())).thenReturn(jwtToken);


    }


    @Test
    void onUserCreation_onValidInput_thenReturn200() throws Exception {
        registerUserVM = new RegisterUserVM(
                "firstName",
                "lastName",
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
                "firstName",
                "lastName",
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
                "firstName",
                "lastName",
                "password",
                "noMatchingPassword",
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
    void onUserCreation_onInvalidEmail_thenReturn400() throws Exception {
        registerUserVM = new RegisterUserVM(
                "firstName",
                "lastName",
                "password",
                "password",
                "hola"
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
        registerUserVM = new RegisterUserVM(
                "",
                "",
                "password",
                "password",
                "hola@gmail.com"
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
        updateUserVM.setFirstName("first name");
        updateUserVM.setLastName("last name");

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
        when(articleProvider.articlesOf(anyInt(), anyInt(), any(), anyLong(), anyBoolean(), anyString())).thenReturn(
                new Page<>(1, 0, 1, 1, 4, Collections.singletonList(mockArticle), true, false));

        User user = new User();
        user.setBio(NEW_BIO);
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/user/articles")
                .param("pageNumber", "2")
                .param("pageSize", "3")
                .param("status", "DRAFT")
                .param("byPopularity", "false")
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
