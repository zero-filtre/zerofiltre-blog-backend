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
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;
import tech.zerofiltre.blog.infra.providers.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.providers.notification.user.*;
import tech.zerofiltre.blog.infra.security.config.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtConfiguration.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, BasicPasswordVerifierProvider.class, StackOverflowTokenConfiguration.class,
        RestApiStackOverflowProvider.class, UserMailNotificationProvider.class, APIClientConfiguration.class})
class UserControllerIT {

    public static final String EMAIL = "email@toto.fr";
    private static final String TOKEN = "token";
    private static final String PASSWORD = "COmplic$t6d";
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserProvider userProvider;


    @MockBean
    NotifyRegistrationComplete notifyRegistrationComplete;

    @MockBean
    ResendRegistrationConfirmation resendRegistrationConfirmation;

    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    @MockBean
    ConfirmUserRegistration confirmUserRegistration;


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
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(new VerificationToken(new User(), TOKEN)));
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
                .andExpect(jsonPath("$.firstName").value("firstName"));


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

    public String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapperBuilder.build().writeValueAsString(obj);
    }
}
