package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.security.crypto.password.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;

import javax.servlet.http.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserControllerTest {

    @MockBean
    private RegisterUser registerUser;
    @MockBean
    private NotifyRegistrationComplete notifyRegistrationComplete;
    @MockBean
    private ConfirmUserRegistration confirmUserRegistration;
    @MockBean
    private MessageSource sources;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private ResendRegistrationConfirmation resendRegistrationConfirmation;

    @MockBean
    UserProvider userProvider;

    @MockBean
    UserNotificationProvider userNotificationProvider;

    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    @MockBean
    HttpServletRequest request;


    UserController userController;

    RegisterUserVM userVM = new RegisterUserVM();

    @BeforeEach
    void setUp() {
        userController = new UserController(
                userProvider, userNotificationProvider, verificationTokenProvider, sources, passwordEncoder);
    }

    @Test
    void registerUserAccount_MustRegisterUser_ThenNotifyRegistrationComplete() throws UserAlreadyExistException {

        //ACT
        userController.registerUserAccount(userVM, request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userProvider, times(1)).save(any());
        verify(userNotificationProvider, times(1)).notifyRegistrationComplete(any());
    }

    @Test
    void resendRegistrationConfirm_mustNotify() {
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));

        //ACT
        userController.resendRegistrationConfirm("email", request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userNotificationProvider, times(1)).notifyRegistrationComplete(any());

    }

    @Test
    void confirmRegistration() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(new VerificationToken(new User(), "")));

        //ACT
        userController.confirmRegistration("token", request);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofToken(any());
        verify(userProvider, times(1)).save(any());

    }
}