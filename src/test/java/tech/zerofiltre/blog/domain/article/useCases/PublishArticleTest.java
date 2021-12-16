package tech.zerofiltre.blog.domain.article.useCases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

@ExtendWith(SpringExtension.class)
class PublishArticleTest {

    private PublishArticle publishArticle;

    private ArticleProvider articleProvider;
    private UserProvider userProvider;
    private TagProvider tagProvider;


    @BeforeEach
    void init() {
        publishArticle = new PublishArticle(articleProvider, userProvider, tagProvider);
    }

    @Test
    @DisplayName("Must produce a proper published article")
    void mustSetStatusToPublished() {
        //ARRANGE
        Article mockArticle = createMockArticle();

        //ACT
        publishArticle.execute(mockArticle);

        //ASSERT
        Assertions
    }

    private Article createMockArticle() {
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
        Tag java = new Tag(12, "java");
        Tag angular = new Tag(13, "angular");
        Tag springBoot = new Tag(14, "spring-boot");
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