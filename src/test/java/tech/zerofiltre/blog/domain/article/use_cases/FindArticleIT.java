package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

@DataJpaTest
@Import({DBArticleProvider.class, DBTagProvider.class, DBUserProvider.class,
        DBReactionProvider.class
})
class FindArticleIT {

    public static final String DDD = "DDD";
    public static final String TDD = "TDD";
    public static final String CLEAN_CODE = "CLEAN CODE";
    public static final String HEXAGONAL_ARCHITECTURE = "HEXAGONAL ARCHITECTURE";
    public static final String UI_DESIGN = "UI DESIGN";
    public static final String UX_DESIGN = "UX Design";
    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private UserProvider userProvider;

    private MetricsProvider metricsProvider;

    private FindArticle findArticle;

    @BeforeEach
    void init() {
        metricsProvider = new DummyMetricsProvider();
        findArticle = new FindArticle(articleProvider, metricsProvider);
    }

    @Test
    @DisplayName("Must properly return articles from the requested page")
    void mustReturnArticlesFromTheRequestedPage() throws ForbiddenActionException, UnAuthenticatedActionException {


        //ARRANGE
        User user = new User();
        user.setRoles(Collections.singleton("ROLE_ADMIN"));
        user = userProvider.save(user);

        Article ddd = new Article();
        ddd.setTitle(DDD);
        ddd.setStatus(Status.PUBLISHED);

        Article tdd = new Article();
        tdd.setTitle(TDD);
        tdd.setStatus(Status.PUBLISHED);


        Article cleanCode = new Article();
        cleanCode.setTitle(CLEAN_CODE);
        cleanCode.setStatus(Status.DRAFT);
        cleanCode.setAuthor(user);

        Article hexagonalArchitecture = new Article();
        hexagonalArchitecture.setTitle(HEXAGONAL_ARCHITECTURE);
        hexagonalArchitecture.setStatus(Status.DRAFT);
        hexagonalArchitecture.setAuthor(user);

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
        Page<Article> firstPageWithTwoInReview = findArticle.of(new FinderRequest(0, 2, Status.IN_REVIEW, user));
        Page<Article> firstPageWith2Published = findArticle.of(new FinderRequest(0, 2, Status.PUBLISHED, user));
        FinderRequest draftRequest = new FinderRequest(0, 2, Status.DRAFT, user);
        Page<Article> firstPageWith2Drafts = findArticle.of(draftRequest);
        draftRequest.setYours(true);
        Page<Article> firstPageWithYour2Drafts = findArticle.of(draftRequest);
        Page<Article> thirdPageWithThreePublished = findArticle.of(new FinderRequest(2, 3, Status.PUBLISHED, user));

        //ASSERT
        assertThat(firstPageWithTwoInReview.getContent()).hasSize(2);
        assertThat(firstPageWithTwoInReview.getContent().get(0).getTitle()).isEqualTo(UX_DESIGN);
        assertThat(firstPageWithTwoInReview.getContent().get(1).getTitle()).isEqualTo(UI_DESIGN);

        assertThat(firstPageWith2Published.getContent()).hasSize(2);
        assertThat(firstPageWith2Published.getContent().get(0).getTitle()).isEqualTo(TDD);
        assertThat(firstPageWith2Published.getContent().get(1).getTitle()).isEqualTo(DDD);

        assertThat(firstPageWith2Drafts.getContent()).hasSize(2);
        assertThat(firstPageWith2Drafts.getContent().get(0).getTitle()).isEqualTo(HEXAGONAL_ARCHITECTURE);
        assertThat(firstPageWith2Drafts.getContent().get(1).getTitle()).isEqualTo(CLEAN_CODE);

        assertThat(firstPageWithYour2Drafts.getContent()).hasSize(2);
        assertThat(firstPageWithYour2Drafts.getContent().get(0).getTitle()).isEqualTo(HEXAGONAL_ARCHITECTURE);
        assertThat(firstPageWithYour2Drafts.getContent().get(1).getTitle()).isEqualTo(CLEAN_CODE);

        assertThat(thirdPageWithThreePublished.getContent()).isEmpty();


    }
}
