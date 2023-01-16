package tech.zerofiltre.blog.infra.entrypoints.rest.course;

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
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.infra.security.config.*;
import tech.zerofiltre.blog.infra.security.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SectionController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class, DBUserProvider.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class, Slf4jLoggerProvider.class})
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
    SectionProvider sectionProvider;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;

    @MockBean
    SecurityContextManager securityContextManager;

    SectionVM sectionVM = new SectionVM();
    Section mockSection;


    @BeforeEach
    void setUp() throws UserNotFoundException {
        mockSection = ZerofiltreUtils.createMockSections(sectionProvider, true).get(0);
        when(sectionProvider.findById(anyLong())).thenReturn(Optional.ofNullable(mockSection));
        when(sectionProvider.save(any())).thenReturn(mockSection);
        User mockUser = ZerofiltreUtils.createMockUser(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(ZerofiltreUtils.createMockCourse(false, Status.DRAFT,courseProvider, mockUser,Collections.emptyList(),Collections.emptyList())));
        when(securityContextManager.getAuthenticatedUser()).thenReturn(mockUser);

        sectionVM.setTitle("title");
        sectionVM.setPosition(5);
        sectionVM.setContent("this is a content");
        sectionVM.setImage("image");
        sectionVM.setCourseId(4);


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
