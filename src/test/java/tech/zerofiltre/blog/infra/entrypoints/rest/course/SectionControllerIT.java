package tech.zerofiltre.blog.infra.entrypoints.rest.course;

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
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.SectionProvider;
import tech.zerofiltre.blog.domain.course.model.Section;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.PasswordEncoderConfiguration;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.SectionVM;
import tech.zerofiltre.blog.infra.providers.api.config.APIClientConfiguration;
import tech.zerofiltre.blog.infra.providers.api.github.GithubLoginProvider;
import tech.zerofiltre.blog.infra.providers.api.so.StackOverflowLoginProvider;
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
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SectionController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class, DBUserProvider.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class, Slf4jLoggerProvider.class, DBChapterProvider.class})
class SectionControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserJPARepository userJPARepository;

    @MockBean
    CourseProvider courseProvider;

    @MockBean
    StackOverflowLoginProvider stackOverflowLoginProvider;

    @MockBean
    GithubLoginProvider githubLoginProvider;

    @MockBean
    DBVerificationTokenProvider verificationTokenProvider;

    @MockBean
    UserProvider userProvider;

    @MockBean
    ChapterProvider chapterProvider;

    @MockBean
    SectionProvider sectionProvider;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;

    @MockBean
    SecurityContextManager securityContextManager;

    @MockBean
    MetricsProvider metricsProvider;

    SectionVM sectionVM = new SectionVM();
    Section mockSection;


    @BeforeEach
    void setUp() throws UserNotFoundException {
        mockSection = ZerofiltreUtils.createMockSections(sectionProvider, courseProvider, true).get(0);
        when(sectionProvider.findById(anyLong())).thenReturn(Optional.ofNullable(mockSection));
        when(sectionProvider.save(any())).thenReturn(mockSection);
        User mockUser = ZerofiltreUtils.createMockUser(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtils.createMockCourse(false, Status.DRAFT, mockUser,Collections.emptyList(),Collections.emptyList())));
        when(securityContextManager.getAuthenticatedUser()).thenReturn(mockUser);

        sectionVM.setTitle("title");
        sectionVM.setPosition(5);
        sectionVM.setContent("this is a content");
        sectionVM.setImage("image");
        sectionVM.setCourseId(4);


    }

    @Test
    void getSection_whenValidInput_return200OK() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/section/1")
                .contentType(MediaType.APPLICATION_JSON);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(mockSection.getTitle()));
    }

    @Test
    @WithMockUser
    void saveSection_whenValidInput_return200OK() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/section")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapperBuilder.build().writeValueAsString(sectionVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(mockSection.getTitle()));
    }

    @Test
    @WithMockUser
    void sectionOfId_whenValidInput_return200OK() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/section/1")
                .contentType(MediaType.APPLICATION_JSON);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(mockSection.getTitle()));
    }

    @Test
    @WithMockUser
    void updateSection_whenValidInput_return200OK() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/section")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapperBuilder.build().writeValueAsString(sectionVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(mockSection.getTitle()));

    }

    @Test
    @WithMockUser
    void deleteSection_whenValidInput_return200OK() throws Exception {
        //ACT
        RequestBuilder request = MockMvcRequestBuilders.delete("/section/1")
                .contentType(MediaType.APPLICATION_JSON);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }

}
