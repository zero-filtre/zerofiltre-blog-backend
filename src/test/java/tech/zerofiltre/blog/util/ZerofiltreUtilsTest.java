package tech.zerofiltre.blog.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.SectionProvider;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ZerofiltreUtilsTest {

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
    public static final String TEST_SUMMARY = "summary";
    public static final String TEST_THUMBNAIL = "https://i.ibb.co/qpwg6Mv/google.gif";
    public static final String TEST_COURSE_TITLE = "test title";
    public static final String TEST_SECTION_TITLE_1 = "Section 1";
    public static final String TEST_SECTION_CONTENT_1 = "Section 1 content";
    public static final String TEST_SECTION_TITLE_2 = "Section 2";
    public static final String TEST_SECTION_CONTENT_2 = "Section 2 content";
    public static final String TEST_SECTION_TITLE_3 = "Section 3";
    public static final String TEST_SECTION_CONTENT_3 = "Section 3 content";
    public static final String TEST_CHAPTER_TITLE = "test chapter title";

    public static Article createMockArticle(boolean withTagIds) {
        User user = createMockUser(false);
        List<Reaction> reactions = createMockReactions(true, 1, 0, user);
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
        user.setWebsite(ZerofiltreUtils.ROOT_URL);
        if (isAdmin)
            user.getRoles().add("ROLE_ADMIN");
        user.setLoginFrom(SocialLink.Platform.LINKEDIN);
        return user;
    }

    public static User createMockProUser(boolean isAdmin, boolean isPro) {
        User user = createMockUser(isAdmin);
        user.setPlan(isPro ? User.Plan.PRO : User.Plan.BASIC);
        return user;
    }

    public static Course createMockCourse(boolean withId, Status status, User author, List<Section> sections, List<Reaction> reactions, boolean isMentored) {
        Course result = createMockCourse(withId, status, author, sections, reactions);
        result.setMentored(isMentored);
        return result;
    }

    public static Course createMockCourse(boolean withId, Status status, User author, List<Section> sections, List<Reaction> reactions) {
        Course course = new Course();
        course.setId(withId ? 45 : 0);
        course.setTitle(TEST_COURSE_TITLE);
        course.setThumbnail(TEST_THUMBNAIL);

        course.setCreatedAt(LocalDateTime.now().minusDays(50));
        course.setStatus(status);
        course.setPublishedAt(status == Status.PUBLISHED ? LocalDateTime.now().minusDays(50) : null);
        course.setAuthor(author);
        course.setReactions(reactions);
        course.setEnrolledCount(10);
        course.setPrice(3599);
        course.setSections(sections);
        course.setSummary(TEST_SUMMARY);
        course.setSandboxType(Sandbox.Type.K8S);
        return course;

    }
    public static Course createMockCourse(Sandbox.Type sandboxType){
        Course course = createMockCourse(false, Status.PUBLISHED, createMockUser(false), Collections.emptyList(), Collections.emptyList());
        course.setSandboxType(sandboxType);
        return course;
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

    public static List<Section> createMockSections(SectionProvider sectionProvider, CourseProvider courseProvider, boolean withSectionIds) {
        Section section1 = new Section.SectionBuilder()
                .title(TEST_SECTION_TITLE_1)
                .content(TEST_SECTION_CONTENT_1)
                .id(withSectionIds ? 1 : 0)
                .image(TEST_THUMBNAIL)
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .position(1)
                .build();
        Section section2 = new Section.SectionBuilder()
                .title(TEST_SECTION_TITLE_2)
                .content(TEST_SECTION_CONTENT_2)
                .id(withSectionIds ? 2 : 0)
                .image(TEST_THUMBNAIL)
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .position(2)
                .build();
        Section section3 = new Section.SectionBuilder()
                .title(TEST_SECTION_TITLE_3)
                .content(TEST_SECTION_CONTENT_3)
                .id(withSectionIds ? 3 : 0)
                .image(TEST_THUMBNAIL)
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .position(3)
                .build();
        return Arrays.asList(section1, section2, section3);
    }

    public static Section createMockSection(long courseId, SectionProvider sectionProvider, CourseProvider courseProvider, boolean withSectionIds) {
        return new Section.SectionBuilder()
                .title(TEST_SECTION_TITLE_3)
                .content(TEST_SECTION_CONTENT_3)
                .courseId(courseId)
                .id(withSectionIds ? 3 : 0)
                .image(TEST_THUMBNAIL)
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .position(3)
                .build();
    }

    public static List<Reaction> createMockReactions(boolean withReactionIds, long articleId, long courseId, User author) {
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
            reaction.setCourseId(courseId);
            reaction.setAuthorId(author.getId());
        });
        return result;

    }

    public static Purchase createMockPurchase(long id, User user, Course course, LocalDateTime localDateTime) {
        Purchase purchase = new Purchase();
        purchase.setId(id);
        purchase.setUser(user);
        purchase.setCourse(course);
        purchase.setAt(localDateTime);
        return purchase;
    }

    public static Chapter createMockChapter(boolean withId, ChapterProvider chapterProvider, List<Lesson> lessons, long courseId) {
        return createMockChapter(withId, chapterProvider, null, null, null, lessons, courseId);
    }

    public static Chapter createMockChapter(boolean withId, ChapterProvider chapterProvider, UserProvider userProvider, LessonProvider lessonProvider, CourseProvider courseProvider, List<Lesson> lessons, long courseId) {
        return Chapter.builder()
                .title(TEST_CHAPTER_TITLE)
                .courseId(courseId)
                .lessons(lessons)
                .id(withId ? 1 : 0)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .courseProvider(courseProvider)
                .build();
    }

    public static Enrollment createMockEnrollment(boolean withId, User user, Course course) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(withId ? 1 : 0);
        enrollment.setUser(user);
        enrollment.setCourse(course);
        return enrollment;

    }

    @Test
    @DisplayName("When a user has a valid email then I return this email")
    void shouldReturnEmail_whenGetValidEmail_withValidEmail() {
        //ARRANGE
        User user = new User();
        user.setEmail("user@email.com");

        //ACT
        String response = ZerofiltreUtils.getValidEmail(user);

        //ASSERT
        assertThat(response).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("When a user has a valid payment email then I return this payment email")
    void shouldReturnPaymentEmail_whenGetValidEmail_withValidPaymentEmail() {
        //ARRANGE
        User user = new User();
        user.setPaymentEmail("user@email.com");

        //ACT
        String response = ZerofiltreUtils.getValidEmail(user);

        //ASSERT
        assertThat(response).isEqualTo(user.getPaymentEmail());
    }

    @Test
    @DisplayName("When a user doesn't have an email or a valid payment email then I return null")
    void shouldReturnBlank_whenGetValidEmail_withNotValidEmailOrPaymentEmail() {
        //ARRANGE
        User user = new User();

        //ACT
        String response = ZerofiltreUtils.getValidEmail(user);

        //ASSERT
        assertThat(response).isBlank();
    }

    @Test
    @DisplayName("When a user has a valid email for broadcast then I return this email")
    void shouldReturnEmail_whenGetValidEmailForBroadcast_withValidEmail() {
        //ARRANGE
        UserForBroadcast user = new UserForBroadcast(1, "user@email.com", "", "", "");

        //ACT
        Optional<String> response = ZerofiltreUtils.getValidEmailForBroadcast(user);

        //ASSERT
        assertThat(response).isNotEmpty();
        assertThat(response.get()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("When a user has a valid payment email for broadcast then I return this email")
    void shouldReturnEmail_whenGetValidEmailForBroadcast_withValidPaymentEmail() {
        //ARRANGE
        UserForBroadcast user = new UserForBroadcast(1, "", "user@email.com", "", "");

        //ACT
        Optional<String> response = ZerofiltreUtils.getValidEmailForBroadcast(user);

        //ASSERT
        assertThat(response).isNotEmpty();
        assertThat(response.get()).isEqualTo(user.getPaymentEmail());
    }

    @Test
    @DisplayName("When a user doesn't have an valid email or a valid payment email for broadcast then I return empty")
    void shouldReturnEmpty_whenGetValidEmailForBroadcast_withNotValidEmailOrPaymentEmail() {
        //ARRANGE
        UserForBroadcast user = new UserForBroadcast();

        //ACT
        Optional<String> response = ZerofiltreUtils.getValidEmailForBroadcast(user);

        //ASSERT
        assertThat(response).isEmpty();
    }

    @Test
    void getBeginningAndEndOfMonthDates() {
        //ARRANGE
        //ACT
        List<LocalDateTime> list = ZerofiltreUtils.getBeginningAndEndOfMonthDates();

        //ASSERT
        assertThat(list.get(0)).isBefore(list.get(1));
    }

    @Test
    void partitionList() {
        //ARRANGE
        List<Integer> listInteger = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        int partitionSize = 4;

        //ACT
        Collection<List<Integer>> list = ZerofiltreUtils.partitionList(listInteger, partitionSize);

        //ASSERT
        assertThat(list.size()).isEqualTo(4);
        assertThat(list.contains(Arrays.asList(1, 2, 3, 4))).isTrue();
        assertThat(list.contains(Arrays.asList(5, 6, 7, 8))).isTrue();
        assertThat(list.contains(Arrays.asList(9, 10, 11, 12))).isTrue();
        assertThat(list.contains(Arrays.asList(13, 14, 15))).isTrue();
        assertThat(list.contains(Arrays.asList(13, 14, 15, 16))).isFalse();
    }

    @Test
    void sanitizeFilename() {
        //ARRANGE
        List<String> fileNamesList = Arrays.asList("a&&a", "bééb", "c//c", "d\\\\d", "e::e", "f**f", "g??g", "h\"\"h", "i<<i", "j>>j", "k||k", "l  l");
        List<String> expectedFileNamesList = Arrays.asList("a&&a", "bééb", "c_c", "d_d", "e_e", "f_f", "g_g", "h_h", "i_i", "j_j", "k_k", "l_l");

        //ACT
        List<String> responses = new ArrayList<>();
        for (String s : fileNamesList) {
            responses.add(ZerofiltreUtils.sanitizeString(s));
        }

        //ASSERT
        assertThat(responses).isEqualTo(expectedFileNamesList);
    }

}