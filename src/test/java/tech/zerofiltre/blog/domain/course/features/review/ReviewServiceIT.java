package tech.zerofiltre.blog.domain.course.features.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBReviewProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;

@DataJpaTest
@Import({DBReviewProvider.class, DBChapterProvider.class, DBUserProvider.class, DBCourseProvider.class, DBEnrollmentProvider.class})
public class ReviewServiceIT {
    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ReviewProvider reviewProvider;

    @Autowired
    private ChapterProvider chapterProvider;

    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private EnrollmentProvider enrollmentProvider;

    private ReviewService reviewService;
    private Review review;
    private User author;
    private User reviewer;
    private Chapter chapter;
    private Course course;
    private Enrollment enrollment;

    public static final String UPDATED_CHAPTER_EXPLANATION = "UPDATED CHAPTER EXPLANATION";
    public static final int UPDATED_CHAPTER_SATISFACTION_SCORE = 5;

    @BeforeEach
    void setup() {
        reviewService = new ReviewService(reviewProvider, userProvider, enrollmentProvider, courseProvider);
    }

    @Test
    void init_review_is_Ok_when_has_enrollement() throws ZerofiltreException {
        // GIVEN
        author = ZerofiltreUtilsTest.createMockUser(false);
        author.setEmail("author@zerofiltre.tech");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        course = courseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList()));

        chapter = chapterProvider.save(ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId()));

        reviewer = userProvider.save(ZerofiltreUtilsTest.createMockUser(false));

        enrollment = enrollmentProvider.save(ZerofiltreUtilsTest.createMockEnrollment(false, reviewer, course));

        review = new Review();
        review.setAuthorId(reviewer.getId());
        review.setChapterId(chapter.getId());
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);
        review.setChapterImpressions("");
        review.setWhyRecommendingThisCourse("");
        review.setFavoriteLearningToolOfTheChapter(null);
        review.setReasonFavoriteLearningToolOfTheChapter("");
        review.setImprovementSuggestion("");

        // WHEN
        Review response = reviewService.init(review);

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isGreaterThan(0);
        assertThat(response.getAuthorId()).isEqualTo(reviewer.getId());
        assertThat(response.getChapterId()).isEqualTo(chapter.getId());
        assertThat(response.getCourseId()).isEqualTo(course.getId());
        assertThat(response.getFavoriteLearningToolOfTheChapter()).isEmpty();
        assertThat(response.getChapterExplanations()).isEqualTo("Great chapter");
        assertThat(response.getChapterSatisfactionScore()).isEqualTo(5);
        assertThat(response.getChapterUnderstandingScore()).isEqualTo(5);
        assertThat(response.isRecommendCourse()).isTrue();
        assertThat(response.getOverallChapterSatisfaction()).isEqualTo(5);
        assertThat(response.getChapterImpressions()).isEqualTo("");
        assertThat(response.getWhyRecommendingThisCourse()).isEqualTo("");
        assertThat(response.getFavoriteLearningToolOfTheChapter()).isEmpty();
        assertThat(response.getReasonFavoriteLearningToolOfTheChapter()).isEqualTo("");
        assertThat(response.getImprovementSuggestion()).isEqualTo("");
    }


    @Test
    void get_review_is_Ok() throws ZerofiltreException {
        // GIVEN
        author = ZerofiltreUtilsTest.createMockUser(false);
        author.setEmail("author@zerofiltre.tech");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        course = courseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList()));

        chapter = chapterProvider.save(ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId()));

        reviewer = userProvider.save(ZerofiltreUtilsTest.createMockUser(false));

        enrollment = enrollmentProvider.save(ZerofiltreUtilsTest.createMockEnrollment(false, reviewer, course));

        List<String> learningTool = new ArrayList<>();
        learningTool.add("Vidéos explicatives");
        learningTool.add("Exercices pratiques");

        String chapterImpression = "La methodologie est claire et digeste";
        String whyDoYouRecommendIt = "Ce cours est magnifique";
        String reasons = "Les vidéos sont bient editées";
        String improvementSuggestion = "Faites davantage de contenu de qualité";

        review = new Review();
        review.setId(4L);
        review.setAuthorId(reviewer.getId());
        review.setChapterId(chapter.getId());
        review.setCourseId(course.getId());
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);
        review.setChapterImpressions(chapterImpression);
        review.setWhyRecommendingThisCourse(whyDoYouRecommendIt);
        review.setFavoriteLearningToolOfTheChapter(learningTool);
        review.setReasonFavoriteLearningToolOfTheChapter(reasons);
        review.setImprovementSuggestion(improvementSuggestion);

        review = reviewProvider.save(review);

        // WHEN
        Optional<Review> foundReview = reviewProvider.findById(review.getId());

        // THEN
        assertThat(foundReview).isNotEmpty();
        assertThat(foundReview.get().getId()).isGreaterThan(0);
        assertThat(foundReview.get().getAuthorId()).isEqualTo(reviewer.getId());
        assertThat(foundReview.get().getChapterId()).isEqualTo(chapter.getId());
        assertThat(foundReview.get().getCourseId()).isEqualTo(course.getId());
        assertThat(foundReview.get().getChapterExplanations()).isEqualTo("Great chapter");
        assertThat(foundReview.get().getChapterSatisfactionScore()).isEqualTo(5);
        assertThat(foundReview.get().getChapterUnderstandingScore()).isEqualTo(5);
        assertThat(foundReview.get().isRecommendCourse()).isTrue();
        assertThat(foundReview.get().getOverallChapterSatisfaction()).isEqualTo(5);
        assertThat(foundReview.get().getChapterImpressions()).isEqualTo(chapterImpression);
        assertThat(foundReview.get().getWhyRecommendingThisCourse()).isEqualTo(whyDoYouRecommendIt);
        assertThat(foundReview.get().getFavoriteLearningToolOfTheChapter()).isEqualTo(learningTool);
        assertThat(foundReview.get().getReasonFavoriteLearningToolOfTheChapter()).isEqualTo(reasons);
        assertThat(foundReview.get().getImprovementSuggestion()).isEqualTo(improvementSuggestion);
    }

    @Test
    void user_has_active_enrollment_update_review_is_Ok() throws ZerofiltreException {
        // GIVEN
        User author = ZerofiltreUtilsTest.createMockUser(false);
        author.setEmail("authorAndAdmin@zerofiltre.tech");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        course = courseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList()));

        chapter = chapterProvider.save(ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId()));

        reviewer = userProvider.save(ZerofiltreUtilsTest.createMockUser(false));

        enrollment = enrollmentProvider.save(ZerofiltreUtilsTest.createMockEnrollment(false, reviewer, course));

        review = new Review();
        review.setAuthorId(reviewer.getId());
        review.setChapterId(chapter.getId());
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);

        review = reviewProvider.save(review);

        // WHEN
        review.setChapterExplanations(UPDATED_CHAPTER_EXPLANATION);
        review.setChapterSatisfactionScore(UPDATED_CHAPTER_SATISFACTION_SCORE);

        Review updatedReview = reviewService.update(review.getId(), reviewer.getId(), review);

        // THEN
        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getChapterExplanations()).isEqualTo(UPDATED_CHAPTER_EXPLANATION);
        assertThat(updatedReview.getChapterSatisfactionScore()).isEqualTo(UPDATED_CHAPTER_SATISFACTION_SCORE);
    }

    @Test
    void delete_review_is_Ok_when_admin_user() throws ZerofiltreException {
        // GIVEN
        User author = ZerofiltreUtilsTest.createMockUser(false);
        author.setEmail("authorAndAdmin@zerofiltre.tech");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        course = courseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList()));

        chapter = chapterProvider.save(ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId()));

        reviewer = userProvider.save(ZerofiltreUtilsTest.createMockUser(true));

        enrollment = enrollmentProvider.save(ZerofiltreUtilsTest.createMockEnrollment(false, reviewer, course));

        review = new Review();
        review.setId(2L);
        review.setAuthorId(reviewer.getId());
        review.setChapterId(chapter.getId());
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);

        review = reviewProvider.save(review);

        assertThat(review).isNotNull();

        // WHEN
        reviewService.delete(review.getId(), review.getAuthorId());

        // THEN
        Optional<Review> reviewFound = reviewProvider.findById(2L);
        assertThat(reviewFound).isEmpty();
    }

}

