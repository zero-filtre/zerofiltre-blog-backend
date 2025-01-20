package tech.zerofiltre.blog.infra.entrypoints.rest.course;

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
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.features.course.CourseService;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.PasswordEncoderConfiguration;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.PublishOrSaveCourseVM;
import tech.zerofiltre.blog.infra.providers.api.config.APIClientConfiguration;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.api.so.StackOverflowLoginProvider;
import tech.zerofiltre.blog.infra.providers.database.company.CompanyJPARepository;
import tech.zerofiltre.blog.infra.providers.database.company.CompanyUserJPARepository;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyProvider;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyUserProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBVerificationTokenProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.infra.security.config.DBUserDetailsService;
import tech.zerofiltre.blog.infra.security.config.LoginFirstAuthenticationEntryPoint;
import tech.zerofiltre.blog.infra.security.config.RoleRequiredAccessDeniedHandler;
import tech.zerofiltre.blog.infra.security.model.GithubAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.JwtAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.StackOverflowAuthenticationTokenProperties;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CourseController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class, DBUserProvider.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class, Slf4jLoggerProvider.class, DBChapterProvider.class, DBCompanyProvider.class, DBCompanyUserProvider.class, CompanyCourseService.class, CourseService.class})
class CourseControllerIT {

    public static final String TITLE = "THIS IS MY TITLE";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserJPARepository userJPARepository;

    @MockBean
    CompanyJPARepository companyJPARepository;

    @MockBean
    CompanyUserJPARepository companyUserJPARepository;

    @MockBean
    CourseProvider courseProvider;

    @MockBean
    ChapterProvider chapterProvider;

    @MockBean
    StackOverflowLoginProvider stackOverflowLoginProvider;

    @MockBean
    GithubLoginProvider githubLoginProvider;

    @MockBean
    DBVerificationTokenProvider verificationTokenProvider;

    @MockBean
    UserProvider userProvider;

    @MockBean
    LoggerProvider loggerProvider;

    @MockBean
    TagProvider tagProvider;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    MetricsProvider metricsProvider;

    @MockBean
    CompanyCourseService companyCourseService;

    @MockBean
    CompanyCourseProvider companyCourseProvider;

    @MockBean
    EnrollmentProvider enrollmentProvider;

    @MockBean
    DataChecker checker;

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;

    PublishOrSaveCourseVM publishOrSaveCourseVM = new PublishOrSaveCourseVM();


    User author = ZerofiltreUtils.createMockUser(false);
    Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(),Collections.emptyList());

    @BeforeEach
    void setUp() {
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(author));
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(author));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));
        when(courseProvider.save(any())).thenReturn(mockCourse);
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(new Tag()));
        doNothing().when(loggerProvider).log(any());

        publishOrSaveCourseVM.setTitle(TITLE);
        publishOrSaveCourseVM.setSections(Collections.emptyList());
        publishOrSaveCourseVM.setSummary("This is my summary, just adding some letters to make sure it reaches  characters");
        publishOrSaveCourseVM.setTags(Collections.emptyList());
        publishOrSaveCourseVM.setSubTitle("This is my subtitle");

    }

    @Test
    @WithMockUser
    void onCourseInit_whenValidInput_thenReturn200() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/course")
                .param("title", TITLE);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(mockCourse.getTitle()));

    }

    @Test
    @WithMockUser
    void onCompanyCourseInit_whenValidInput_thenReturn200() throws Exception {
        //ARRANGE
        when(checker.companyExists(anyLong())).thenReturn(true);
        when(checker.isAdminOrCompanyUser(any(User.class), anyLong())).thenReturn(true);
        when(checker.isCompanyAdminOrCompanyEditor(any(User.class), anyLong())).thenReturn(true);

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/course")
                .param("title", TITLE)
                .param("companyId", String.valueOf(1));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(mockCourse.getTitle()));


    }

    @Test
    @WithMockUser
    void onCoursePublish_whenValidInput_thenReturn200() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/course/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveCourseVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void onCourseSave_whenValidInput_thenReturn200() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveCourseVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    public String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapperBuilder.build().writeValueAsString(obj);
    }

    @Test
    void findById_whenValidInput_Returns200() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/course/1");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(mockCourse.getTitle()))
                .andExpect(jsonPath("$.id").value(mockCourse.getId()));
    }
}
