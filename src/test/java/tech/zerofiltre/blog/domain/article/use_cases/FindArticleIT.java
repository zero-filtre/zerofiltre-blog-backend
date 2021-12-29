package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
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
    void mustReturnArticlesFromTheRequestedPage() {


        //ARRANGE
        Article ddd = new Article();
        ddd.setTitle(DDD);

        Article tdd = new Article();
        tdd.setTitle(TDD);

        Article cleanCode = new Article();
        cleanCode.setTitle(CLEAN_CODE);

        Article hexagonalArchitecture = new Article();
        hexagonalArchitecture.setTitle(HEXAGONAL_ARCHITECTURE);

        Article uiDesign = new Article();
        uiDesign.setTitle(UI_DESIGN);

        Article uxDesign = new Article();
        uxDesign.setTitle(UX_DESIGN);

        articleProvider.save(ddd);
        articleProvider.save(tdd);
        articleProvider.save(cleanCode);
        articleProvider.save(hexagonalArchitecture);
        articleProvider.save(uiDesign);
        articleProvider.save(uxDesign);


        //ACT
        List<Article> firstPageWithTwoElements = findArticle.of(0, 2);
        List<Article> secondPageWithThreeElements = findArticle.of(1, 3);
        List<Article> thirdPageWithTwoElements = findArticle.of(2, 2);
        List<Article> thirdPageWithThreeElement = findArticle.of(2, 3);

        //ASSERT
        assertThat(firstPageWithTwoElements).hasSize(2);
        assertThat(firstPageWithTwoElements.get(0).getTitle()).isEqualTo(DDD);
        assertThat(firstPageWithTwoElements.get(1).getTitle()).isEqualTo(TDD);

        assertThat(secondPageWithThreeElements).hasSize(3);
        assertThat(secondPageWithThreeElements.get(0).getTitle()).isEqualTo(HEXAGONAL_ARCHITECTURE);
        assertThat(secondPageWithThreeElements.get(1).getTitle()).isEqualTo(UI_DESIGN);
        assertThat(secondPageWithThreeElements.get(2).getTitle()).isEqualTo(UX_DESIGN);

        assertThat(thirdPageWithTwoElements).hasSize(2);
        assertThat(thirdPageWithTwoElements.get(0).getTitle()).isEqualTo(UI_DESIGN);
        assertThat(thirdPageWithTwoElements.get(1).getTitle()).isEqualTo(UX_DESIGN);

        assertThat(thirdPageWithThreeElement).isEmpty();


    }
}
