package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.features.FindArticle;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.features.course.CourseService;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.features.*;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.mapper.PublicUserProfileVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.mapper.UpdateUserVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.logging.SpringMessageSourceProvider;
import tech.zerofiltre.blog.infra.security.model.Token;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

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
    private final InfraProperties infraProperties;
    private final RetrieveSocialToken retrieveSocialToken;
    private final DeleteUser deleteUser;
    private final UpdateUserVMMapper updateUserVMMapper = Mappers.getMapper(UpdateUserVMMapper.class);
    private final PublicUserProfileVMMapper publicUserProfileVMMapper = Mappers.getMapper(PublicUserProfileVMMapper.class);
    private final UpdateUser updateUser;
    private final UserProvider userProvider;
    private final FindArticle findArticle;
    private final GenerateToken generateToken;
    private final CourseService courseService;


    public UserController(UserProvider userProvider, MetricsProvider metricsProvider, UserNotificationProvider userNotificationProvider, ArticleProvider articleProvider, VerificationTokenProvider verificationTokenProvider, MessageSource sources, PasswordEncoder passwordEncoder, SecurityContextManager securityContextManager, PasswordVerifierProvider passwordVerifierProvider, InfraProperties infraProperties, GithubLoginProvider githubLoginProvider, AvatarProvider profilePictureGenerator, VerificationTokenProvider tokenProvider, ReactionProvider reactionProvider, JwtTokenProvider jwtTokenProvider, LoggerProvider loggerProvider, TagProvider tagProvider, CourseProvider courseProvider, ArticleViewProvider articleViewProvider, DataChecker checker, CompanyCourseProvider companyCourseProvider) {
        this.userProvider = userProvider;
        this.registerUser = new RegisterUser(userProvider, profilePictureGenerator, metricsProvider);
        this.notifyRegistrationComplete = new NotifyRegistrationComplete(userNotificationProvider);
        this.sources = sources;
        this.passwordEncoder = passwordEncoder;
        this.infraProperties = infraProperties;
        this.updateUser = new UpdateUser(userProvider);
        this.findArticle = new FindArticle(articleProvider, metricsProvider, articleViewProvider);
        this.updatePassword = new UpdatePassword(userProvider, passwordVerifierProvider, metricsProvider);
        this.securityContextManager = securityContextManager;
        this.savePasswordReset = new SavePasswordReset(verificationTokenProvider, userProvider);
        this.confirmUserRegistration = new ConfirmUserRegistration(verificationTokenProvider, userProvider);
        this.resendRegistrationConfirmation = new ResendRegistrationConfirmation(userProvider, userNotificationProvider, tokenProvider);
        this.initPasswordReset = new InitPasswordReset(userProvider, userNotificationProvider, tokenProvider, new SpringMessageSourceProvider(sources));
        this.verifyToken = new VerifyToken(verificationTokenProvider);
        this.retrieveSocialToken = new RetrieveSocialToken(githubLoginProvider);
        this.deleteUser = new DeleteUser(userProvider, articleProvider, tokenProvider, reactionProvider, courseProvider, loggerProvider);
        this.generateToken = new GenerateToken(verificationTokenProvider, jwtTokenProvider, userProvider);
        this.courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider);

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
        Token token = generateToken.byUser(user);


        String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
        try {
            notifyRegistrationComplete.execute(user, token.getRefreshToken(), request.getLocale(), appUrl);
        } catch (RuntimeException e) {
            log.error("We were unable to send the registration confirmation email", e);
        }

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
    public Page<Article> getArticles(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam String status, @RequestParam(required = false) String filter, @RequestParam(required = false) String tag) throws UserNotFoundException, ForbiddenActionException, UnAuthenticatedActionException {
        User user = securityContextManager.getAuthenticatedUser();
        FinderRequest request = new FinderRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setUser(user);
        request.setTag(tag);
        if (filter != null) {
            filter = filter.toUpperCase();
            request.setFilter(FinderRequest.Filter.valueOf(filter));
        }
        if (status != null) {
            status = status.toUpperCase();
            request.setStatus(Status.valueOf(status));
        }
        request.setYours(true);
        return findArticle.of(request);
    }

    @GetMapping("/user/courses")
    public Page<Course> getCourses(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam String status, @RequestParam(required = false) String filter, @RequestParam(required = false) String tag) throws UserNotFoundException, ForbiddenActionException, UnAuthenticatedActionException {
        User user = securityContextManager.getAuthenticatedUser();

        FinderRequest request = new FinderRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setUser(user);
        request.setTag(tag);
        if (filter != null) {
            filter = filter.toUpperCase();
            request.setFilter(FinderRequest.Filter.valueOf(filter));
        }
        if (status != null) {
            status = status.toUpperCase();
            request.setStatus(Status.valueOf(status));
        }
        request.setYours(true);
        return courseService.of(request);
    }

    @PatchMapping("/user")
    public User updateUser(@RequestBody @Valid UpdateUserVM updateUserVM) throws ZerofiltreException {
        User user = updateUserVMMapper.fromVM(updateUserVM);
        User currentUser = securityContextManager.getAuthenticatedUser();
        return updateUser.patch(currentUser, user);
    }


    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable("id") long userId, HttpServletRequest request) throws ZerofiltreException {
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
    public String initPasswordReset(@RequestParam String email, HttpServletRequest request) throws ForbiddenActionException{
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
    public String updatePassword(@RequestBody @Valid UpdatePasswordVM passwordVM, HttpServletRequest request) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        String newEncodedPassword = passwordEncoder.encode(passwordVM.getPassword());
        updatePassword.execute(user.getEmail(), passwordVM.getOldPassword(), newEncodedPassword);
        return sources.getMessage("message.reset.password.success", null, request.getLocale());

    }

    @PostMapping("/user/updateEmail")
    public String updateEmail(@RequestBody @Valid EmailHolder emailVM, HttpServletRequest request) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        updateUser.execute(emailVM.getEmail(), user);
        return sources.getMessage("message.reset.email.success", null, request.getLocale());

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
