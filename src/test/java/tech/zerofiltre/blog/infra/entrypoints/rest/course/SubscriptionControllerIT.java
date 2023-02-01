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
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.infra.security.config.*;
import tech.zerofiltre.blog.infra.security.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubscriptionController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class, DBUserProvider.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class, Slf4jLoggerProvider.class, DBChapterProvider.class})
class SubscriptionControllerIT {
    @MockBean
    SecurityContextManager securityContextManager;
    @MockBean
    ChapterProvider chapterProvider;
    @MockBean
    CourseProvider courseProvider;
    @MockBean
    UserProvider userProvider;
    @MockBean
    LessonProvider lessonProvider;
    @MockBean
    LoggerProvider loggerProvider;
    @MockBean
    SubscriptionProvider subscriptionProvider;
    @MockBean
    UserJPARepository userJPARepository;
    @MockBean
    StackOverflowLoginProvider stackOverflowLoginProvider;
    @MockBean
    GithubLoginProvider githubLoginProvider;
    @MockBean
    DBVerificationTokenProvider verificationTokenProvider;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;
    User author = ZerofiltreUtils.createMockUser(false);
    Course mockCourse = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws UserNotFoundException {
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(author));
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(author));
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(mockCourse));
        when(securityContextManager.getAuthenticatedUser()).thenReturn((author));
        when(chapterProvider.chapterOfId(anyLong())).thenReturn(Optional.of(Chapter.builder().courseId(mockCourse.getId()).build()));
        when(lessonProvider.lessonOfId(anyLong())).thenReturn(Optional.of(Lesson.builder().build()));
        when(subscriptionProvider.subscriptionOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(new Subscription()));
        when(subscriptionProvider.save(any())).thenAnswer(i -> {
            Subscription subscription = (Subscription) i.getArguments()[0];
            subscription.setCourse(mockCourse);
            return subscription;
        });
        doNothing().when(loggerProvider).log(any());
    }


    @Test
    @WithMockUser
    void onSubscribe_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.post("/subscription?courseId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void onCompleted_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.patch("/subscription/complete?courseId=45&lessonId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void onUnCompleted_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.patch("/subscription/uncomplete?courseId=45&lessonId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void onUnsubscribe_whenValidInput_thenReturns200() throws Exception {

        //when
        mockMvc.perform(MockMvcRequestBuilders.delete("/subscription?courseId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }
}
