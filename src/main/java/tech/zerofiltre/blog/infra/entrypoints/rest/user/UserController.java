package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import lombok.extern.slf4j.*;
import org.springframework.context.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;
import tech.zerofiltre.blog.util.*;

import javax.servlet.http.*;
import javax.validation.*;
import java.util.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final RegisterUser registerUser;
    private final NotifyRegistrationComplete notifyRegistrationComplete;
    private final ConfirmUserRegistration confirmUserRegistration;
    private final MessageSource sources;
    private final PasswordEncoder passwordEncoder;
    private final ResendRegistrationConfirmation resendRegistrationConfirmation;
    private final InitPasswordReset initPasswordReset;
    private final VerifyToken verifyToken;
    private final SavePasswordReset savePasswordReset;
    private final UpdatePassword updatePassword;
    private final SecurityContextManager securityContextManager;

    public UserController(UserProvider userProvider, UserNotificationProvider userNotificationProvider, VerificationTokenProvider verificationTokenProvider, MessageSource sources, PasswordEncoder passwordEncoder, SecurityContextManager securityContextManager, PasswordVerifierProvider passwordVerifierProvider) {
        this.registerUser = new RegisterUser(userProvider);
        this.notifyRegistrationComplete = new NotifyRegistrationComplete(userNotificationProvider);
        this.sources = sources;
        this.passwordEncoder = passwordEncoder;
        this.updatePassword = new UpdatePassword(userProvider,passwordVerifierProvider);
        this.securityContextManager = securityContextManager;
        this.savePasswordReset = new SavePasswordReset(verificationTokenProvider, userProvider);
        this.confirmUserRegistration = new ConfirmUserRegistration(verificationTokenProvider, userProvider);
        this.resendRegistrationConfirmation = new ResendRegistrationConfirmation(userProvider, userNotificationProvider);
        this.initPasswordReset = new InitPasswordReset(userProvider, userNotificationProvider);
        this.verifyToken = new VerifyToken(verificationTokenProvider);
    }

    @PostMapping
    public User registerUserAccount(@RequestBody @Valid RegisterUserVM registerUserVM, HttpServletRequest request) throws UserAlreadyExistException {
        User user = new User();
        user.setFirstName(registerUserVM.getFirstName());
        user.setLastName(registerUserVM.getLastName());
        user.setEmail(registerUserVM.getEmail());
        user.setPassword(passwordEncoder.encode(registerUserVM.getPassword()));
        user = registerUser.execute(user);

        String appUrl = ZerofiltreUtils.getAppURL(request);

        try {
            notifyRegistrationComplete.execute(user, appUrl, request.getLocale());
        } catch (RuntimeException e) {
            log.error("We were unable to send the registration confirmation email", e);
        }
        return user;
    }

    @GetMapping("/resendRegistrationConfirm")
    public String resendRegistrationConfirm(@RequestParam String email, HttpServletRequest request) {
        String appUrl = ZerofiltreUtils.getAppURL(request);
        try {
            resendRegistrationConfirmation.execute(email, appUrl, request.getLocale());
        } catch (UserNotFoundException e) {
            log.error("We were unable to re-send the registration confirmation email", e);
        }
        return sources.getMessage("message.registration.resent", null, request.getLocale());
    }


    @GetMapping("/registrationConfirm")
    public String registrationConfirm(@RequestParam String token, HttpServletRequest request) throws InvalidTokenException {
        confirmUserRegistration.execute(token);
        return sources.getMessage("message.account.validated", null, request.getLocale());

    }

    @GetMapping("/initPasswordReset")
    public String initPasswordReset(@RequestParam String email, HttpServletRequest request) {
        String appUrl = ZerofiltreUtils.getAppURL(request);
        try {
            initPasswordReset.execute(email, appUrl, request.getLocale());
        } catch (UserNotFoundException e) {
            log.error("We were unable to initiate password reset", e);
        }
        return sources.getMessage("message.reset.password.sent", null, request.getLocale());
    }

    @GetMapping("/verifyTokenForPasswordReset")
    public Map<String, String> verifyTokenForPasswordReset(@RequestParam String token) throws InvalidTokenException {
        verifyToken.execute(token);
        return Collections.singletonMap("token", token);
    }

    @PostMapping("/savePasswordReset")
    public String savePasswordReset(@RequestBody @Valid ResetPasswordVM passwordVM, HttpServletRequest request) throws InvalidTokenException {
        savePasswordReset.execute(passwordEncoder.encode(passwordVM.getPassword()), passwordVM.getToken());
        return sources.getMessage("message.reset.password.success", null, request.getLocale());
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@RequestBody @Valid UpdatePasswordVM passwordVM, HttpServletRequest request) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        String oldEncodedPassword = passwordEncoder.encode(passwordVM.getOldPassword());
        String newEncodedPassword = passwordEncoder.encode(passwordVM.getPassword());
        updatePassword.execute(user.getEmail(), oldEncodedPassword, newEncodedPassword);
        return sources.getMessage("message.reset.password.success", null, request.getLocale());

    }
}
