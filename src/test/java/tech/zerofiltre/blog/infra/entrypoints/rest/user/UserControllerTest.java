package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.features.FindArticle;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.features.course.CourseService;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.features.InvalidTokenException;
import tech.zerofiltre.blog.domain.user.features.RetrieveSocialToken;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.JwtToken;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;
import tech.zerofiltre.blog.doubles.DummyMetricsProvider;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.PublicUserProfileVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.RegisterUserVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.UpdatePasswordVM;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.infra.security.model.JwtAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.Token;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.user.model.SocialLink.Platform.GITHUB;

@ExtendWith(SpringExtension.class)
class UserControllerTest {

    public static final String TOKEN = "token";
    public static final String EMAIL = "email";
    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String BIO = "bio";
    public static final String GITHUBLINK = "Github link";
    public static final String WEBSITE = "website";
    @MockBean
    MessageSource sources;

    @MockBean
    FindArticle findArticle;

    @Mock
    RetrieveSocialToken retrieveSocialToken;

    @MockBean
    PasswordEncoder passwordEncoder;

    @MockBean
    UserProvider userProvider;
    @MockBean
    AvatarProvider profilePictureGenerator;
    @MockBean
    GithubLoginProvider githubLoginProvider;
    @MockBean
    ArticleProvider articleProvider;
    @MockBean
    JwtAuthenticationTokenProperties jwTokenConfiguration;
    LoggerProvider loggerProvider = new Slf4jLoggerProvider();
    @MockBean
    PasswordVerifierProvider passwordVerifierProvider;
    @MockBean
    UserNotificationProvider userNotificationProvider;
    @MockBean
    VerificationTokenProvider verificationTokenProvider;
    @MockBean
    Environment environment;
    @MockBean
    HttpServletRequest request;
    @MockBean
    InfraProperties infraProperties;
    UserController userController;
    RegisterUserVM userVM = new RegisterUserVM();
    UpdatePasswordVM updatePasswordVM = new UpdatePasswordVM();
    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);
    @MockBean
    private ReactionProvider reactionProvider;
    @MockBean
    private SecurityContextManager securityContextManager;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    TagProvider tagProvider;

    @MockBean
    ChapterProvider chapterProvider;

    @MockBean
    CourseProvider courseProvider;

    @MockBean
    private ArticleViewProvider articleViewProvider;

    @MockBean
    private DataChecker checker;

    @MockBean
    private CompanyCourseService companyCourseService;

    @Mock
    private CourseService courseService;

    private MetricsProvider metricsProvider;

    @BeforeEach
    void setUp() {
        metricsProvider = new DummyMetricsProvider();
        userController = new UserController(
                userProvider, metricsProvider, userNotificationProvider, articleProvider, verificationTokenProvider, sources,
                passwordEncoder, securityContextManager, passwordVerifierProvider,
                infraProperties, githubLoginProvider, profilePictureGenerator, verificationTokenProvider, reactionProvider, jwtTokenProvider, loggerProvider, tagProvider, courseProvider, articleViewProvider, checker, companyCourseService);

        when(infraProperties.getEnv()).thenReturn("dev");
    }

    @Test
    void registerUser_MustRegisterUser_ThenNotifyRegistrationComplete() throws ResourceAlreadyExistException {
        //ARRANGE
        userVM.setEmail(EMAIL);
        userVM.setFullName(FIRST_NAME);
        when(request.getLocale()).thenReturn(Locale.FRANCE);
        VerificationToken t = new VerificationToken(new User(), TOKEN, expiryDate);
        when(verificationTokenProvider.generate(any())).thenReturn(t);
        when(verificationTokenProvider.generate(any(), anyLong())).thenReturn(t);
        when(jwtTokenProvider.generate(any())).thenReturn(new JwtToken(TOKEN, 784587));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT
        userController.registerUser(userVM, request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userProvider, times(1)).save(captor.capture());
        User user = captor.getValue();
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getFullName()).isEqualTo(FIRST_NAME);
        assertThat(user.getLanguage()).isEqualTo(Locale.FRANCE.getLanguage());
        verify(userNotificationProvider, times(1)).notify(any());
        verify(verificationTokenProvider, times(1)).generate(any());
        verify(jwtTokenProvider, times(1)).generate(any());
    }

    @Test
    void resendRegistrationConfirm_mustNotify() {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));
        VerificationToken t = new VerificationToken(new User(), TOKEN, expiryDate);
        when(verificationTokenProvider.generate(any(), anyLong())).thenReturn(t);

        //ACT
        userController.resendRegistrationConfirm("email", request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userNotificationProvider, times(1)).notify(any());

    }

    @Test
    void confirmRegistration_mustCheckToken_ThenSaveUser() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(new VerificationToken(new User(), "", expiryDate)));

        //ACT
        userController.registrationConfirm("token", request);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofToken(any());
        verify(userProvider, times(1)).save(any());

    }

    @Test
    void resetPassword_mustCheckUser_ThenNotify() throws ForbiddenActionException{
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));
        VerificationToken t = new VerificationToken(new User(), TOKEN, expiryDate);
        when(verificationTokenProvider.generate(any(), anyLong())).thenReturn(t);
        //ACT
        userController.initPasswordReset("email", request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(EMAIL);
        verify(userNotificationProvider, times(1)).notify(any());

    }

    @Test
    void verifyToken_mustCheckToken() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(new VerificationToken(new User(), "", expiryDate)));


        //ACT
        userController.verifyTokenForPasswordReset(TOKEN);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofToken(any());

    }

    @Test
    void updatePassword_mustCheckUserAndPassword_thenSave() throws ZerofiltreException {
        //ARRANGE
        User user = new User();
        user.setEmail(EMAIL);
        when(securityContextManager.getAuthenticatedUser()).thenReturn(user);
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));
        when(passwordVerifierProvider.isValid(any(), any())).thenReturn(true);
        when(userProvider.save(any())).thenReturn(user);

        //ACT
        userController.updatePassword(updatePasswordVM, request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(EMAIL);
        verify(passwordVerifierProvider, times(1)).isValid(user, updatePasswordVM.getOldPassword());
        verify(userProvider, times(1)).save(any());

    }

    @Test
    void getUserProfile_mustBuildPublicUserProfileVM_properly() throws UserNotFoundException {
        //ARRANGE
        User user = new User();
        user.setFullName(FIRST_NAME);
        user.setBio(BIO);
        user.setLanguage(Locale.FRANCE.getLanguage());
        user.setSocialLinks(Collections.singleton(new SocialLink(GITHUB, GITHUBLINK)));
        user.setWebsite(WEBSITE);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        //ACT
        PublicUserProfileVM result = userController.getUserProfile(23);

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo(FIRST_NAME);
        assertThat(result.getBio()).isEqualTo(BIO);
        assertThat(result.getLanguage()).isEqualTo(Locale.FRANCE.getLanguage());
        Set<SocialLink> socialLinks = result.getSocialLinks();
        socialLinks.forEach(socialLink -> {
            assertThat(socialLink.getLink()).isEqualTo(GITHUBLINK);
            assertThat(socialLink.getPlatform()).isEqualTo(GITHUB);
        });
        assertThat(result.getWebsite()).isEqualTo(WEBSITE);

    }

    @Test
    void getUserProfile_throwUserNotFoundException_IfNoUserExists() {
        //ARRANGE
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userController.getUserProfile(23));


    }

    @Test
    void retrieveSocialToken_constructTheToken() throws ResourceNotFoundException {
        //ARRANGE
        ReflectionTestUtils.setField(userController, "retrieveSocialToken", retrieveSocialToken);
        when(retrieveSocialToken.execute(any())).thenReturn(TOKEN);

        //ACT
        Token token = userController.getGithubToken("code");
        assertThat(token.getTokenType()).isEqualTo("token");
        assertThat(token.getAccessToken()).isEqualTo(TOKEN);
        assertThat(token.getRefreshToken()).isEmpty();
        assertThat(token.getAccessTokenExpiryDateInSeconds()).isZero();
        assertThat(token.getRefreshTokenExpiryDateInSeconds()).isZero();

        //ASSERT
        verify(retrieveSocialToken, times(1)).execute("code");

    }

    @Test
    void getArticle_constructFindArticleRequest_Properly() throws UserNotFoundException, UnAuthenticatedActionException, ForbiddenActionException {


        ReflectionTestUtils.setField(userController, "findArticle", findArticle);
        when(findArticle.of(any())).thenReturn(null);


        userController.getArticles(2, 2, "PUBLISHED", "MOST_VIEWED", "java");

        ArgumentCaptor<FinderRequest> argument = ArgumentCaptor.forClass(FinderRequest.class);
        verify(findArticle, times(1)).of(argument.capture());
        FinderRequest request = argument.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getFilter()).isEqualTo(FinderRequest.Filter.MOST_VIEWED);
        assertThat(request.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(request.getTag()).isEqualTo("java");
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(2);

    }

    @Test
    void getCourse_constructsRequest_Properly() throws UserNotFoundException, UnAuthenticatedActionException, ForbiddenActionException {
        ReflectionTestUtils.setField(userController, "courseService", courseService);

        when(courseService.of(any())).thenReturn(null);
        userController.getCourses(2, 2, "PUBLISHED", "MOST_VIEWED", "java");

        ArgumentCaptor<FinderRequest> argument = ArgumentCaptor.forClass(FinderRequest.class);
        verify(courseService, times(1)).of(argument.capture());
        FinderRequest request = argument.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getFilter()).isEqualTo(FinderRequest.Filter.MOST_VIEWED);
        assertThat(request.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(request.getTag()).isEqualTo("java");
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(2);

    }

    @Test
    void getArticle_constructFindArticleRequest_Properly_withNullFilter() throws UserNotFoundException, UnAuthenticatedActionException, ForbiddenActionException {


        ReflectionTestUtils.setField(userController, "findArticle", findArticle);
        when(findArticle.of(any())).thenReturn(null);


        userController.getArticles(2, 2, "PUBLISHED", null, "java");

        ArgumentCaptor<FinderRequest> argument = ArgumentCaptor.forClass(FinderRequest.class);
        verify(findArticle, times(1)).of(argument.capture());
        FinderRequest request = argument.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getFilter()).isNull();
        assertThat(request.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(request.getTag()).isEqualTo("java");
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(2);

    }

    @Test
    void getCourse_constructsFinderRequest_Properly_withNullFilter() throws UserNotFoundException, UnAuthenticatedActionException, ForbiddenActionException {

        ReflectionTestUtils.setField(userController, "courseService", courseService);
        when(courseService.of(any())).thenReturn(null);

        userController.getCourses(2, 2, "PUBLISHED", null, "java");

        ArgumentCaptor<FinderRequest> argument = ArgumentCaptor.forClass(FinderRequest.class);
        verify(courseService, times(1)).of(argument.capture());
        FinderRequest request = argument.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getFilter()).isNull();
        assertThat(request.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(request.getTag()).isEqualTo("java");
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(2);

    }

    @Test
    void getArticle_constructFindArticleRequest_Properly_withNullStatus() throws UserNotFoundException, UnAuthenticatedActionException, ForbiddenActionException {

        ReflectionTestUtils.setField(userController, "findArticle", findArticle);
        when(findArticle.of(any())).thenReturn(null);


        userController.getArticles(2, 2, null, "MOST_VIEWED", "java");

        ArgumentCaptor<FinderRequest> argument = ArgumentCaptor.forClass(FinderRequest.class);
        verify(findArticle, times(1)).of(argument.capture());
        FinderRequest request = argument.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getFilter()).isEqualTo(FinderRequest.Filter.MOST_VIEWED);
        assertThat(request.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(request.getTag()).isEqualTo("java");
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(2);

    }

    @Test
    void getCourse_constructsFinderRequest_Properly_withNullStatus() throws UserNotFoundException, UnAuthenticatedActionException, ForbiddenActionException {

        ReflectionTestUtils.setField(userController, "courseService", courseService);
        when(courseService.of(any())).thenReturn(null);


        userController.getCourses(2, 2, null, "MOST_VIEWED", "java");

        ArgumentCaptor<FinderRequest> argument = ArgumentCaptor.forClass(FinderRequest.class);
        verify(courseService, times(1)).of(argument.capture());
        FinderRequest request = argument.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getFilter()).isEqualTo(FinderRequest.Filter.MOST_VIEWED);
        assertThat(request.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(request.getTag()).isEqualTo("java");
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(2);

    }
}