package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

@DataJpaTest
@Import({ArticleDatabaseProvider.class, TagDatabaseProvider.class, UserDatabaseProvider.class, ReactionDatabaseProvider.class})
class FindArticleIT {

    public static final String DDD = "DDD";
    public static final String TDD = "TDD";
    public static final String CLEAN_CODE = "CLEAN CODE";
    public static final String HEXAGONAL_ARCHITECTURE = "HEXAGONAL ARCHITECTURE";
    public static final String UI_DESIGN = "UI DESIGN";
    public static final String UX_DESIGN = "UX Design";
    @Autowired
    private ArticleProvider articleProvider;

    private FindArticle findArticle;

    @BeforeEach
    void init() {
        findArticle = new FindArticle(articleProvider);
    }

    @Test
    @DisplayName("Must properly return articles from the requested page")
    void mustReturnArticlesFromTheRequestedPage() throws ForbiddenActionException {


        //ARRANGE
        User user = new User();
        user.setRoles(Collections.singleton("ROLE_ADMIN"));

        Article ddd = new Article();
        ddd.setTitle(DDD);
        ddd.setStatus(Status.PUBLISHED);

        Article tdd = new Article();
        tdd.setTitle(TDD);
        tdd.setStatus(Status.PUBLISHED);


        Article cleanCode = new Article();
        cleanCode.setTitle(CLEAN_CODE);
        cleanCode.setStatus(Status.DRAFT);

        Article hexagonalArchitecture = new Article();
        hexagonalArchitecture.setTitle(HEXAGONAL_ARCHITECTURE);
        hexagonalArchitecture.setStatus(Status.DRAFT);

        Article uiDesign = new Article();
        uiDesign.setTitle(UI_DESIGN);
        uiDesign.setStatus(Status.IN_REVIEW);

        Article uxDesign = new Article();
        uxDesign.setTitle(UX_DESIGN);
        uxDesign.setStatus(Status.IN_REVIEW);

        articleProvider.save(ddd);
        articleProvider.save(tdd);
        articleProvider.save(cleanCode);
        articleProvider.save(hexagonalArchitecture);
        articleProvider.save(uiDesign);
        articleProvider.save(uxDesign);


        //ACT
        List<Article> firstPageWithTwoInReview = findArticle.of(new FindArticleRequest(0, 2, Status.IN_REVIEW, user));
        List<Article> firstPageWith2Published = findArticle.of(new FindArticleRequest(0, 2, Status.PUBLISHED, user));
        List<Article> firstPageWith2Drafts = findArticle.of(new FindArticleRequest(0, 2, Status.DRAFT, user));
        List<Article> thirdPageWithThreePublished = findArticle.of(new FindArticleRequest(2, 3, Status.PUBLISHED, user));

        //ASSERT
        assertThat(firstPageWithTwoInReview).hasSize(2);
        assertThat(firstPageWithTwoInReview.get(0).getTitle()).isEqualTo(UX_DESIGN);
        assertThat(firstPageWithTwoInReview.get(1).getTitle()).isEqualTo(UI_DESIGN);

        assertThat(firstPageWith2Published).hasSize(2);
        assertThat(firstPageWith2Published.get(0).getTitle()).isEqualTo(TDD);
        assertThat(firstPageWith2Published.get(1).getTitle()).isEqualTo(DDD);

        assertThat(firstPageWith2Drafts).hasSize(2);
        assertThat(firstPageWith2Drafts.get(0).getTitle()).isEqualTo(HEXAGONAL_ARCHITECTURE);
        assertThat(firstPageWith2Drafts.get(1).getTitle()).isEqualTo(CLEAN_CODE);

        assertThat(thirdPageWithThreePublished).isEmpty();


    }
}
