package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.*;
import org.springframework.http.converter.json.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleController.class)
class ArticleControllerTest {

    public static final String TITLE = "Des applications très évolutives alignées aux derniers standards.";
    @MockBean
    ArticleProvider articleProvider;

    @MockBean
    UserProvider userProvider;

    @MockBean
    TagProvider tagProvider;

    @MockBean
    ReactionProvider reactionProvider;


    Article mockArticle = ZerofiltreUtils.createMockArticle(true);
    PublishOrSaveArticleVM publishOrSaveArticleVM = new PublishOrSaveArticleVM(
            mockArticle.getId(),
            mockArticle.getTitle(),
            mockArticle.getThumbnail(),
            mockArticle.getContent(),
            mockArticle.getTags()
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Jackson2ObjectMapperBuilder objectMapperBuilder;

    @BeforeEach
    void init() {
        //ARRANGE
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(mockArticle.getAuthor()));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(mockArticle.getTags().get(0)));
        when(articleProvider.save(any())).thenReturn(mockArticle);
        when(articleProvider.articlesOf(anyInt(), anyInt())).thenReturn(Collections.singletonList(mockArticle));
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle));
        when(reactionProvider.reactionOfId(anyLong())).thenReturn(Optional.ofNullable(mockArticle.getReactions().get(0)));

    }


    @Test
    void onArticleInit_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.post("/article")
                .param("title", TITLE);

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));

    }

    @Test
    void onArticlePublish_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/article/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveArticleVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));

    }

    @Test
    void onArticleSave_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.patch("/article")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(publishOrSaveArticleVM));

        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));

    }


    @Test
    void onArticleById_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/article/12");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(TITLE));

    }

    @Test
    void onArticleCards_whenValidInput_thenReturn200() throws Exception {


        //ACT
        RequestBuilder request = MockMvcRequestBuilders.get("/article/list")
                .param("pageNumber", "2")
                .param("pageSize", "3");


        //ASSERT
        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value(TITLE));

    }

    public String asJsonString(final Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = objectMapperBuilder.build();
        return objectMapper.writeValueAsString(obj);
    }


}