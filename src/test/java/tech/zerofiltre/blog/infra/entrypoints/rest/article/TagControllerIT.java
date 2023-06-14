package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import com.fasterxml.jackson.core.*;
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
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.security.config.*;
import tech.zerofiltre.blog.infra.security.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TagController.class)
@Import({Jackson2ObjectMapperBuilder.class, DBUserDetailsService.class, JwtAuthenticationTokenProperties.class,
        LoginFirstAuthenticationEntryPoint.class, RoleRequiredAccessDeniedHandler.class, PasswordEncoderConfiguration.class,
        InfraProperties.class, SecurityContextManager.class, StackOverflowAuthenticationTokenProperties.class,
        APIClientConfiguration.class, GithubAuthenticationTokenProperties.class})
class TagControllerIT {

    public static final String COLOR_CODE = "#fffff";
    public static final String NAME = "name";

    @MockBean
    TagProvider tagProvider;


    @MockBean
    StackOverflowLoginProvider loginProvider;

    @MockBean
    GithubLoginProvider githubLoginProvider;

    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UserProvider userProvider;

    @MockBean
    MetricsProvider metricsProvider;


    List<Tag> tags = ZerofiltreUtils.createMockTags(true);

    @Autowired
    Jackson2ObjectMapperBuilder objectMapperBuilder;

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void init() {
        when(tagProvider.tags()).thenReturn(tags);
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.ofNullable(tags.get(0)));
    }


    @Test
    void onTags_whenValidInput_thenReturn200() throws Exception {

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/tag");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(tags.get(0).getName()));

        verify(tagProvider, times(1)).tags();

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void onCreateTag_whenValidInput_savesProperly_thenReturn200() throws Exception {
        //ARRANGE
        EditTagVM tagVM = new EditTagVM();
        tagVM.setColorCode(COLOR_CODE);
        tagVM.setName(NAME);
        when(tagProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/admin/tag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(tagVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("name").value(NAME));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void onUpdateTag_whenValidInput_thenReturn200() throws Exception {
        EditTagVM tagVM = new EditTagVM();
        tagVM.setId(5);
        tagVM.setColorCode(COLOR_CODE);
        tagVM.setName(NAME);

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/admin/tag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(tagVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void onCreateTag_whenInValidInput_thenReturn400() throws Exception {
        EditTagVM tagVM = new EditTagVM();
        tagVM.setId(5);
        tagVM.setName(NAME);

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/admin/tag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(tagVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void onUpdateTag_whenInValidInput_thenReturn400() throws Exception {
        EditTagVM tagVM = new EditTagVM();
        tagVM.setId(5);
        tagVM.setName(NAME);

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/admin/tag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(tagVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());

    }

    @Test
    void onTagOfId_whenValidInput_thenReturn200() throws Exception {

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/tag/12");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("java"));
    }

    @Test
    void onTagOfId_whenInvalidInput_thenReturn400() throws Exception {

        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/tag/:12");

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

    public String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapperBuilder.build().writeValueAsString(obj);
    }

}
