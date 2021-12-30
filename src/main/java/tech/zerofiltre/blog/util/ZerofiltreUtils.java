package tech.zerofiltre.blog.util;

import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

public class ZerofiltreUtils {

    private ZerofiltreUtils() {
    }

    public static Article createMockArticle(boolean withTagIds) {
        User user = createMockUser();
        List<Reaction> reactions = createMockReactions(true, 1, user);
        List<Tag> tags = createMockTags(withTagIds);
        return createMockArticle(user, tags, reactions);
    }

    public static Article createMockArticle(User user, List<Tag> tags, List<Reaction> reactions) {
        Article mockArticle = new Article();
        mockArticle.setId(1);
        mockArticle.setCreatedAt(LocalDateTime.now());
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


        mockArticle.setThumbnail("https://i.ibb.co/TbFN2zC/landing-illustration.png");
        mockArticle.setAuthor(user);
        mockArticle.setContent(content);
        mockArticle.setReactions(reactions);
        mockArticle.setStatus(Status.DRAFT);
        mockArticle.setTitle("Des applications très évolutives alignées aux derniers standards.");

        mockArticle.setTags(tags);
        return mockArticle;
    }

    public static User createMockUser() {
        User user = new User();
        user.setId(1);
        user.setFirstName("Philippe");
        user.setLastName("GUEMKAM SIMO");
        user.setPseudoName("imphilippesimo");
        user.setRegisteredOn(LocalDateTime.now().minusDays(50));
        user.setProfilePicture("https://i.ibb.co/QKX6gyr/profile-pic.jpg");
        Set<SocialLink> socialLinks = new HashSet<>(Arrays.asList(
                new SocialLink(SocialLink.Platform.GITHUB, "https://github.com/imphilippesimo"),
                new SocialLink(SocialLink.Platform.STACKOVERFLOW, "https://stackoverflow.com/users/5615357/philippe-simo"),
                new SocialLink(SocialLink.Platform.TWITTER, "https://twitter.com/imphilippesimo"),
                new SocialLink(SocialLink.Platform.LINKEDIN, "https://www.linkedin.com/in/philippesimo/")
        ));
        user.setBio("Je développe des Applications qui boostent votre business. Suivez-moi \uD83D\uDCA1\n" +
                "\n" +
                "Ma dévise: L'amélioration continue. \n" +
                "Une application sert le business et pas le contraire. ");
        user.setFunction("Senior Java Developer");
        user.setSocialLinks(socialLinks);
        user.setWebsite("https://zerofiltre.tech");
        return user;
    }

    public static List<Tag> createMockTags(boolean withTagIds) {
        Tag java = new Tag();
        java.setName("java");
        Tag angular = new Tag();
        angular.setName("angular");
        Tag springBoot = new Tag();
        springBoot.setName("spring-boot");
        if (withTagIds) {
            java.setId(12);
            angular.setId(13);
            springBoot.setId(14);
        }

        return Arrays.asList(java, angular, springBoot);
    }

    public static List<Reaction> createMockReactions(boolean withReactionIds, long articleId, User author) {
        Reaction clap = new Reaction();
        clap.setAction(Reaction.Action.CLAP);
        Reaction like = new Reaction();
        like.setAction(Reaction.Action.LIKE);
        Reaction love = new Reaction();
        love.setAction(Reaction.Action.LOVE);
        Reaction fire = new Reaction();
        fire.setAction(Reaction.Action.FIRE);
        Reaction fire2 = new Reaction();
        fire2.setAction(Reaction.Action.FIRE);
        if (withReactionIds) {
            clap.setId(11);
            like.setId(25);
            fire.setId(35);
            fire2.setId(47);
        }
        List<Reaction> result = Arrays.asList(clap, like, love, fire, fire2);
        result.forEach(reaction -> {
            reaction.setArticleId(articleId);
            reaction.setAuthor(author);
        });
        return result;

    }
}
