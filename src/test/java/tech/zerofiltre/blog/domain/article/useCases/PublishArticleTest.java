package tech.zerofiltre.blog.domain.article.useCases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class PublishArticleTest {

    private PublishArticle publishArticle;

    private ArticleProvider articleProvider;
    private UserProvider userProvider;
    private TagProvider tagProvider;

    Article article = new Article();
    User user = new User();
    Tag tag = new Tag();

    @BeforeEach
    void init(){
        publishArticle = new PublishArticle(articleProvider,userProvider,tagProvider);
    }

    @Test
    @DisplayName("Must produce a proper published article")
    void mustSetStatusToPublished(){
        //ARRANGE

        //ACT

        //ASSERT
    }
}