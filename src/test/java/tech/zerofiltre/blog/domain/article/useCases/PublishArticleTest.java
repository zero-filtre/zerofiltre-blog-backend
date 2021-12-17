package tech.zerofiltre.blog.domain.article.useCases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@ExtendWith(SpringExtension.class)
class PublishArticleTest {

    private PublishArticle publishArticle;

    @MockBean
    private ArticleProvider articleProvider;

    @BeforeEach
    void init() {
        publishArticle = new PublishArticle(articleProvider);
    }

    @Test
    @DisplayName("Must set the status to published then save the article")
    void mustSetStatusToPublished() {
        //ARRANGE
        LocalDateTime beforePublication = LocalDateTime.now();
        Article mockArticle = createMockArticle(true);
        mockArticle.setId(45);
        when(articleProvider.save(any())).thenReturn(mockArticle);

        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotNull();

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(publishedArticle.getPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastPublishedAt()).isNotNull();
        assertThat(publishedArticle.getPublishedAt()).isBeforeOrEqualTo(publishedArticle.getLastPublishedAt());
        assertThat(publishedArticle.getLastPublishedAt()).isAfterOrEqualTo(beforePublication);

        assertThat(publishedArticle.getAuthor()).isEqualTo(mockArticle.getAuthor());
        assertThat(publishedArticle.getContent()).isEqualTo(mockArticle.getContent());
        assertThat(publishedArticle.getThumbnail()).isEqualTo(mockArticle.getThumbnail());
        assertThat(publishedArticle.getTitle()).isEqualTo(mockArticle.getTitle());
        assertThat(publishedArticle.getTags()).hasSameElementsAs(mockArticle.getTags());
        assertThat(publishedArticle.getReactions()).hasSameElementsAs(mockArticle.getReactions());
        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must register article if it is not yet register")
    void mustRegisterWhenPublishing() {
        //ARRANGE
        Article mockArticle = createMockArticle(true);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article result = invocationOnMock.getArgument(0);
            result.setId(45);
            return result;
        });

        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotNull();
    }

    @Test
    @DisplayName("Must save tags")
    void mustSaveTags() {
        //ARRANGE
        Article mockArticle = createMockArticle(false);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article article = invocationOnMock.getArgument(0);
            article.getTags().forEach(tag -> tag.setId(4));
            return article;
        });

        //ACT
        Article publishedArticle = publishArticle.execute(mockArticle);

        //ASSERT
        verify(articleProvider, times(1)).save(mockArticle);
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getTags().size()).isEqualTo(3);
        publishedArticle.getTags().forEach(tag -> assertThat(tag.getId()).isNotNull());
    }

    private Article createMockArticle(boolean withTagIds) {
        Article mockArticle = new Article();
        User user = new User();
        String content = "<div class=\"our-service__box\">\n" +
                "          <div class=\"our-service__text\">\n" +
                "            <h1 class=\"our-service__title\">Des applications très évolutives alignées aux derniers standards.</h1>\n" +
                "            <div class=\"text-box wrap-text\">\n" +
                "              <p class=\"our-service__description\">De par sa nature stable et sécurisée, le langage de programmation\n" +
                "                <b>JAVA</b> est\n" +
                "                utilisé par de nombreuses entreprises opérant dans des secteurs tels: la banque, l’assurance, les\n" +
                "                transports etc, et ce depuis\n" +
                "                plus de 25 ans.<br><br>\n" +
                "                Avec JAVA, le code est écrit une et une seule fois et peut être utilisé\n" +
                "                sur des systèmes d'exploitation différents, sans avoir à être réécrit. Il se positionne\n" +
                "                alors comme l’une des solutions incontournables à adopter pour automatiser les tâches lourdes du\n" +
                "                business,\n" +
                "                fluidifier le\n" +
                "                processus métier et répondre mieux au besoin du client afin d’accélérer sa croissance.\n" +
                "              </p>\n" +
                "              <p class=\"our-service__description moreText hide-mobile-text show-desktop-text\">Zerofiltre a pour objectif\n" +
                "                de vous\n" +
                "                accompagner,\n" +
                "                particuliers, moyennes\n" +
                "                et petites entreprises dans ce processus, en vous\n" +
                "                fournissant des applications en JAVA, compatibles pour toutes plateformes, qui pourront évoluer avec la\n" +
                "                taille de votre\n" +
                "                entreprise, avec un rapport qualité/prix toujours au top.\n" +
                "                Nous nous améliorons en continu afin de fournir aux clients, un service meilleur que celui dont ils\n" +
                "                s'imaginent.<br><br>\n" +
                "                Cependant, Le langage JAVA\n" +
                "                utilise une méthodologie stricte et bien élaborée qui nécessite des années d’expérience : c’est\n" +
                "                exactement ce dont nous disposons.\n" +
                "              </p>\n" +
                "            </div>\n" +
                "            <span class=\"view-more show-mobile hide-desktop\">\n" +
                "              <small>Voir plus</small>\n" +
                "              <img src=\"/images/dropdown-icon.svg\" alt=\"chevron\" id=\"toggleChevron\">\n" +
                "            </span>\n" +
                "          </div>\n" +
                "\n" +
                "          <div class=\"our-service__img\">\n" +
                "            <img src=\"https://i.ibb.co/TbFN2zC/landing-illustration.png\" alt=\"our-service\" class=\"img-fluid\">\n" +
                "          </div>\n" +
                "        </div>";
        List<Reaction> reactions = Arrays.asList(Reaction.CLAP, Reaction.CLAP, Reaction.FIRE, Reaction.FIRE, Reaction.LOVE);
        Tag java = new Tag();
        java.setName("java");
        Tag angular = new Tag();
        angular.setName("angular");
        Tag springBoot = new Tag();
        springBoot.setName("java");
        if (withTagIds) {
            java.setId(12);
            angular.setId(13);
            springBoot.setId(14);
        }

        List<Tag> tags = Arrays.asList(java, angular, springBoot);
        mockArticle.setThumbnail("https://i.ibb.co/TbFN2zC/landing-illustration.png");
        mockArticle.setAuthor(user);
        mockArticle.setContent(content);
        mockArticle.setReactions(reactions);
        mockArticle.setStatus(Status.DRAFT);
        mockArticle.setTitle("Des applications très évolutives alignées aux derniers standards.");
        mockArticle.setTags(tags);
        return mockArticle;
    }
}