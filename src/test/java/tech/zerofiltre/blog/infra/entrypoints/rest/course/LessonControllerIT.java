//package tech.zerofiltre.blog.infra.entrypoints.rest.course;
//
//import com.fasterxml.jackson.core.*;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.*;
//import org.springframework.boot.test.autoconfigure.web.servlet.*;
//import org.springframework.boot.test.mock.mockito.*;
//import org.springframework.context.annotation.*;
//import org.springframework.http.*;
//import org.springframework.http.converter.json.*;
//import org.springframework.security.test.context.support.*;
//import org.springframework.test.web.servlet.*;
//import org.springframework.test.web.servlet.request.*;
//import tech.zerofiltre.blog.domain.article.*;
//import tech.zerofiltre.blog.domain.article.model.Tag;
//import tech.zerofiltre.blog.domain.article.model.*;
//import tech.zerofiltre.blog.domain.course.*;
//import tech.zerofiltre.blog.domain.course.model.*;
//import tech.zerofiltre.blog.domain.logging.*;
//import tech.zerofiltre.blog.domain.user.*;
//import tech.zerofiltre.blog.domain.user.model.*;
//import tech.zerofiltre.blog.infra.*;
//import tech.zerofiltre.blog.infra.entrypoints.rest.*;
//import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;
//import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;
//import tech.zerofiltre.blog.infra.providers.api.config.*;
//import tech.zerofiltre.blog.infra.providers.api.github.*;
//import tech.zerofiltre.blog.infra.providers.api.so.*;
//import tech.zerofiltre.blog.infra.providers.database.user.*;
//import tech.zerofiltre.blog.infra.providers.logging.*;
//import tech.zerofiltre.blog.infra.security.config.*;
//import tech.zerofiltre.blog.infra.security.model.*;
//import tech.zerofiltre.blog.util.*;
//
//import java.util.*;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(controllers = LessonController.class)
//@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
//        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
//        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class, DBUserProvider.class,
//        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class, Slf4jLoggerProvider.class})
//class LessonControllerIT {
//
//    public static final String TITLE = "THIS IS MY TITLE";
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    UserJPARepository userJPARepository;
//
//    @MockBean
//    CourseProvider courseProvider;
//
//    @MockBean
//    StackOverflowLoginProvider stackOverflowLoginProvider;
//
//    @MockBean
//    GithubLoginProvider githubLoginProvider;
//
//    @MockBean
//    DBVerificationTokenProvider verificationTokenProvider;
//
//    @MockBean
//    UserProvider userProvider;
//
//    @MockBean
//    LoggerProvider loggerProvider;
//
//    @MockBean
//    TagProvider tagProvider;
//
//    @MockBean
//    JwtTokenProvider jwtTokenProvider;
//
//    @Autowired
//    Jackson2ObjectMapperBuilder objectMapperBuilder;
//
//    PublishOrSaveCourseVM publishOrSaveCourseVM = new PublishOrSaveCourseVM();
//
//
//    User author = ZerofiltreUtils.createMockUser(false);
//    Course mockCourse = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, courseProvider, author, Collections.emptyList(),Collections.emptyList());
//
//    @BeforeEach
//    void setUp() {
//        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(author));
//        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(author));
//        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));
//        when(courseProvider.save(any())).thenReturn(mockCourse);
//        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(new Tag()));
//        doNothing().when(loggerProvider).log(any());
//
//        publishOrSaveCourseVM.setTitle(TITLE);
//        publishOrSaveCourseVM.setSections(Collections.emptyList());
//        publishOrSaveCourseVM.setSummary("This is my summary, just adding some letters to make sure it reaches  characters");
//        publishOrSaveCourseVM.setTags(Collections.emptyList());
//        publishOrSaveCourseVM.setSubTitle("This is my subtitle");
//
//    }
//
//    @Test
//    @WithMockUser
//    void onCourseInit_whenValidInput_thenReturn200() throws Exception {
//        //ACT
//        RequestBuilder request = MockMvcRequestBuilders.post("/course")
//                .param("title", TITLE);
//
//        //ASSERT
//        mockMvc.perform(request)
//                .andExpect(status().is2xxSuccessful())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.title").value(mockCourse.getTitle()));
//
//
//    }
//
//    @Test
//    @WithMockUser
//    void onCoursePublish_whenValidInput_thenReturn200() throws Exception {
//        //ACT
//        RequestBuilder request = MockMvcRequestBuilders.patch("/course/publish")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(publishOrSaveCourseVM));
//
//        //ASSERT
//        mockMvc.perform(request)
//                .andExpect(status().is2xxSuccessful())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    @WithMockUser
//    void onCourseSave_whenValidInput_thenReturn200() throws Exception {
//        //ACT
//        RequestBuilder request = MockMvcRequestBuilders.patch("/course")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(publishOrSaveCourseVM));
//
//        //ASSERT
//        mockMvc.perform(request)
//                .andExpect(status().is2xxSuccessful())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//
//    public String asJsonString(final Object obj) throws JsonProcessingException {
//        return objectMapperBuilder.build().writeValueAsString(obj);
//    }
//
//    @Test
//    void findById_whenValidInput_Returns200() throws Exception {
//        //ACT
//        RequestBuilder request = MockMvcRequestBuilders.get("/course/1");
//
//        //ASSERT
//        mockMvc.perform(request)
//                .andExpect(status().is2xxSuccessful())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.title").value(mockCourse.getTitle()))
//                .andExpect(jsonPath("$.id").value(mockCourse.getId()));
//    }
//}
