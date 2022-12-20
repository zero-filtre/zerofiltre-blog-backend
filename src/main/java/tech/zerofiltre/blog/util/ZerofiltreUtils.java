package tech.zerofiltre.blog.util;

import lombok.extern.slf4j.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.user.model.*;

import javax.servlet.http.*;
import java.io.*;
import java.security.*;
import java.time.*;
import java.util.*;

@Slf4j
public class ZerofiltreUtils {

    public static final String TEST_BIO = "Je développe des Applications qui boostent votre business. Suivez-moi \uD83D\uDCA1\n" +
            "\n" +
            "Ma dévise: L'amélioration continue. \n" +
            "Une application sert le business et pas le contraire. ";
    public static final String TEST_EMAIL = "ola.eloundou@zerofiltre.tech";
    public static final String TEST_FULL_NAME = "Philippe GUEMKAM SIMO";
    public static final String TEST_PSEUDONAME = "imphilippesimo";
    public static final String TEST_PROFILE_PICTURE = "https://i.ibb.co/QKX6gyr/profile-pic.jpg";
    public static final String TEST_GUTHUB_LINK = "https://github.com/imphilippesimo";
    public static final String TEST_STACKOVERFLOW_LINK = "https://stackoverflow.com/users/5615357/philippe-simo";
    public static final String TEST_LINKEDIN_LINK = "https://www.linkedin.com/in/philippesimo/";
    public static final String TEST_PROFESSION = "Senior Java Developer";
    public static final String ROOT_URL = "https://zerofiltre.tech";
    public static final String TEST_SUMMARY = "summary";
    public static final String TEST_THUMBNAIL = "https://i.ibb.co/qpwg6Mv/google.gif";
    public static final String TEST_COURSE_TITLE = "test title";
    public static final String TEST_SECTION_TITLE_1 = "Section 1";
    public static final String TEST_SECTION_CONTENT_1 = "Section 1 content";
    public static final String TEST_SECTION_TITLE_2 = "Section 2";
    public static final String TEST_SECTION_CONTENT_2 = "Section 2 content";
    public static final String TEST_SECTION_TITLE_3 = "Section 3";
    public static final String TEST_SECTION_CONTENT_3 = "Section 3 content";

    private ZerofiltreUtils() {
    }

    public static Article createMockArticle(boolean withTagIds) {
        User user = createMockUser(false);
        List<Reaction> reactions = createMockReactions(true, 1, user);
        List<Tag> tags = createMockTags(withTagIds);
        return createMockArticle(user, tags, reactions);
    }

    public static Article createMockArticle(User user, List<Tag> tags, List<Reaction> reactions) {
        Article mockArticle = new Article();
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
        mockArticle.setSummary("Appuyez-vous sur la puissance d'un langage utilisé par de nombreuses entreprises opérant dans des secteurs tels: la banque et l’assurance. Mature de plus de 25 ans, sa robustesse n'est plus à prouver.");
        return mockArticle;
    }

    public static User createMockUser(boolean isAdmin) {
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setFullName(TEST_FULL_NAME);
        user.setPseudoName(TEST_PSEUDONAME);
        user.setRegisteredOn(LocalDateTime.now().minusDays(50));
        user.setProfilePicture(TEST_PROFILE_PICTURE);
        Set<SocialLink> socialLinks = new HashSet<>(Arrays.asList(
                new SocialLink(SocialLink.Platform.GITHUB, TEST_GUTHUB_LINK),
                new SocialLink(SocialLink.Platform.STACKOVERFLOW, TEST_STACKOVERFLOW_LINK),
                new SocialLink(SocialLink.Platform.LINKEDIN, TEST_LINKEDIN_LINK)
        ));
        user.setBio(TEST_BIO);
        user.setProfession(TEST_PROFESSION);
        user.setSocialLinks(socialLinks);
        user.setWebsite(ROOT_URL);
        if (isAdmin)
            user.getRoles().add("ROLE_ADMIN");
        user.setLoginFrom(SocialLink.Platform.LINKEDIN);
        return user;
    }

    public static Course createMockCourse(boolean withId, Status status, CourseProvider courseProvider, User author, List<Section> sections) {
        return new Course.CourseBuilder()
                .id(withId ? 45 : 0)
                .title(TEST_COURSE_TITLE)
                .thumbnail(TEST_THUMBNAIL)
                .courseProvider(courseProvider)
                .createdAt(LocalDateTime.now().minusDays(50))
                .status(status)
                .publishedAt(status == Status.PUBLISHED ? LocalDateTime.now().minusDays(50) : null)
                .author(author)
                .enrolledCount(10)
                .price(35.99)
                .sections(sections)
                .summary(TEST_SUMMARY)
                .build();

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

    public static List<Section> createMockSections(SectionProvider sectionProvider, boolean withSectionIds) {
        Section section1 = new Section.SectionBuilder()
                .title(TEST_SECTION_TITLE_1)
                .content(TEST_SECTION_CONTENT_1)
                .id(withSectionIds ? 1 : 0)
                .image(TEST_THUMBNAIL)
                .sectionProvider(sectionProvider)
                .position(1)
                .build();
        Section section2 = new Section.SectionBuilder()
                .title(TEST_SECTION_TITLE_2)
                .content(TEST_SECTION_CONTENT_2)
                .id(withSectionIds ? 2 : 0)
                .image(TEST_THUMBNAIL)
                .sectionProvider(sectionProvider)
                .position(2)
                .build();
        Section section3 = new Section.SectionBuilder()
                .title(TEST_SECTION_TITLE_3)
                .content(TEST_SECTION_CONTENT_3)
                .id(withSectionIds ? 3 : 0)
                .image(TEST_THUMBNAIL)
                .sectionProvider(sectionProvider)
                .position(3)
                .build();
        return Arrays.asList(section1, section2, section3);
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
            reaction.setAuthorId(author.getId());
        });
        return result;

    }


    public static String getAppURL(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    public static String getOriginUrl(String env) {
        return env.equals("prod") ? ROOT_URL : "https://" + env + ".zerofiltre.tech";
    }

    public static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

    public static String md5Hex(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return hex(md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            log.error("We couldn't generate the hex to get the gravatar image", e);
        }
        return null;
    }

}
