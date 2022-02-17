package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import lombok.extern.slf4j.*;
import org.mapstruct.factory.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.mapper.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.security.model.*;
import tech.zerofiltre.blog.util.*;

import javax.servlet.http.*;
import javax.validation.*;
import java.util.*;

@RestController
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
    private final JwtAuthenticationTokenProperties jwTokenConfiguration;
    private final InfraProperties infraProperties;
    private final RetrieveSocialToken retrieveSocialToken;
    private final DeleteUser deleteUser;
    private final UpdateUserVMMapper updateUserVMMapper = Mappers.getMapper(UpdateUserVMMapper.class);
    private final PublicUserProfileVMMapper publicUserProfileVMMapper = Mappers.getMapper(PublicUserProfileVMMapper.class);
    private final UpdateUser updateUser;
    private final UserProvider userProvider;
    private final FindArticle findArticle;
    private final GenerateToken generateToken;


    public UserController(UserProvider userProvider, UserNotificationProvider userNotificationProvider, ArticleProvider articleProvider, VerificationTokenProvider verificationTokenProvider, MessageSource sources, PasswordEncoder passwordEncoder, SecurityContextManager securityContextManager, PasswordVerifierProvider passwordVerifierProvider, JwtAuthenticationTokenProperties jwTokenConfiguration, InfraProperties infraProperties, GithubLoginProvider githubLoginProvider, AvatarProvider profilePictureGenerator, JwtTokenProvider jwtTokenProvider) {
        this.registerUser = new RegisterUser(userProvider, profilePictureGenerator);
        this.notifyRegistrationComplete = new NotifyRegistrationComplete(userNotificationProvider);
        this.sources = sources;
        this.passwordEncoder = passwordEncoder;
        this.jwTokenConfiguration = jwTokenConfiguration;
        this.infraProperties = infraProperties;
        this.updateUser = new UpdateUser(userProvider);
        this.findArticle = new FindArticle(articleProvider);
        this.updatePassword = new UpdatePassword(userProvider, passwordVerifierProvider);
        this.securityContextManager = securityContextManager;
        this.savePasswordReset = new SavePasswordReset(verificationTokenProvider, userProvider);
        this.confirmUserRegistration = new ConfirmUserRegistration(verificationTokenProvider, userProvider);
        this.resendRegistrationConfirmation = new ResendRegistrationConfirmation(userProvider, userNotificationProvider);
        this.initPasswordReset = new InitPasswordReset(userProvider, userNotificationProvider);
        this.verifyToken = new VerifyToken(verificationTokenProvider);
        this.retrieveSocialToken = new RetrieveSocialToken(githubLoginProvider);
        this.deleteUser = new DeleteUser(userProvider);
        this.userProvider = userProvider;
        this.generateToken = new GenerateToken(verificationTokenProvider, jwtTokenProvider, userProvider);
    }

    @PostMapping("/user")
    //TODO Refactor this to delegate the orchestration to the application layer
    public ResponseEntity<Token> registerUser(@RequestBody @Valid RegisterUserVM registerUserVM, HttpServletRequest request) throws ResourceAlreadyExistException {
        User user = new User();
        user.setFullName(registerUserVM.getFullName());
        user.setEmail(registerUserVM.getEmail());
        user.setPassword(passwordEncoder.encode(registerUserVM.getPassword()));
        user.setLanguage(request.getLocale().getLanguage());
        user = registerUser.execute(user);


        String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
        try {
            notifyRegistrationComplete.execute(user, appUrl, request.getLocale());
        } catch (RuntimeException e) {
            log.error("We were unable to send the registration confirmation email", e);
        }

        Token token = generateToken.byUser(user);
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public User getUser() throws UserNotFoundException {
        return securityContextManager.getAuthenticatedUser();
    }

    @GetMapping("/user/profile/{id}")
    public PublicUserProfileVM getUserProfile(@PathVariable long id) throws UserNotFoundException {
        return userProvider.userOfId(id)
                .map(publicUserProfileVMMapper::toVM)
                .orElseThrow(() -> new UserNotFoundException("Unable to find the wanted user", String.valueOf(id)));
    }

    @GetMapping("/user/articles")
    public List<Article> getArticles(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam String status) throws UserNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        status = status.toUpperCase();
        FindArticleRequest request = new FindArticleRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setStatus(Status.valueOf(status));
        request.setUser(user);
        request.setYours(true);
        return findArticle.of(request);
    }

    @PatchMapping("/user")
    public User updateUser(@RequestBody @Valid UpdateUserVM updateUserVM) throws BlogException {
        User user = updateUserVMMapper.fromVM(updateUserVM);
        User currentUser = securityContextManager.getAuthenticatedUser();
        return updateUser.patch(currentUser, user);
    }


    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable("id") long userId, HttpServletRequest request) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        deleteUser.execute(user, userId);
        return sources.getMessage("message.delete.user.success", null, request.getLocale());
    }

    @GetMapping("/user/resendRegistrationConfirm")
    public String resendRegistrationConfirm(@RequestParam String email, HttpServletRequest request) {
        String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
        try {
            resendRegistrationConfirmation.execute(email, appUrl, request.getLocale());
        } catch (UserNotFoundException e) {
            log.error("We were unable to re-send the registration confirmation email", e);
        }
        return sources.getMessage("message.registration.resent", null, request.getLocale());
    }


    @GetMapping("/user/registrationConfirm")
    public String registrationConfirm(@RequestParam String token, HttpServletRequest request) throws InvalidTokenException {
        confirmUserRegistration.execute(token);
        return sources.getMessage("message.account.validated", null, request.getLocale());

    }

    @GetMapping("/user/initPasswordReset")
    public String initPasswordReset(@RequestParam String email, HttpServletRequest request) {
        String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
        try {
            initPasswordReset.execute(email, appUrl, request.getLocale());
        } catch (UserNotFoundException e) {
            log.error("We were unable to initiate password reset", e);
        }
        return sources.getMessage("message.reset.password.sent", null, request.getLocale());
    }

    @GetMapping("/user/verifyTokenForPasswordReset")
    public Map<String, String> verifyTokenForPasswordReset(@RequestParam String token) throws InvalidTokenException {
        verifyToken.execute(token);
        return Collections.singletonMap("token", token);
    }

    @PostMapping("/user/savePasswordReset")
    public String savePasswordReset(@RequestBody @Valid ResetPasswordVM passwordVM, HttpServletRequest request) throws InvalidTokenException {
        savePasswordReset.execute(passwordEncoder.encode(passwordVM.getPassword()), passwordVM.getToken());
        return sources.getMessage("message.reset.password.success", null, request.getLocale());
    }

    @PostMapping("/user/updatePassword")
    public String updatePassword(@RequestBody @Valid UpdatePasswordVM passwordVM, HttpServletRequest request) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        String newEncodedPassword = passwordEncoder.encode(passwordVM.getPassword());
        updatePassword.execute(user.getEmail(), passwordVM.getOldPassword(), newEncodedPassword);
        return sources.getMessage("message.reset.password.success", null, request.getLocale());

    }


    @PostMapping("/user/github/accessToken")
    public Token getGithubToken(@RequestParam String code) throws ResourceNotFoundException {
        String accessToken = retrieveSocialToken.execute(code);
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setTokenType("token");
        token.setRefreshToken("");
        return token;
    }

    @GetMapping("/user/jwt/refreshToken")
    public Token refreshJwtToken(@RequestParam(name = "refreshToken") String refreshingToken) throws InvalidTokenException {
        return this.generateToken.byRefreshToken(refreshingToken);
    }
}
