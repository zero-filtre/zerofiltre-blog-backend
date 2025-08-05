package tech.zerofiltre.blog.domain.course.features.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    private ReviewService reviewService;
    private Review review;
    
    @Mock
    ReviewProvider reviewProvider;

    @Mock
    UserProvider userProvider;

    @Mock
    CourseProvider courseProvider;

    @Mock
    EnrollmentProvider enrollmentProvider;

    @BeforeEach
    void setup() {
        reviewService = new ReviewService(reviewProvider, userProvider, enrollmentProvider, courseProvider);
    }

    @DisplayName("Initialize a review if the user is not found (not connected) THEN a ResourceNotFoundException is thrown")
    @Test
    void init_throws_ResourceNotFoundException_if_user_not_found() {
        // GIVEN
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        // THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reviewService.init(new Review()));
    }

    @DisplayName("WHEN a user wants to init a non-existing review and has active enrollment, the review is saved.")
    @Test
    void should_SaveReview_whenInitNonExistingReview_hasUserWithActiveEnrollment() throws ResourceNotFoundException, ForbiddenActionException {
        // GIVEN
        User reviewer = ZerofiltreUtilsTest.createMockUser(false);
        long courseId = 2L;

        review = new Review();
        review.setAuthorId(reviewer.getId());
        review.setChapterId(1L);
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);

        when(userProvider.userOfId(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(courseProvider.courseIdOfChapterId(review.getChapterId())).thenReturn(courseId);
        when(enrollmentProvider.enrollmentOf(reviewer.getId(), courseId, true)).thenReturn(Optional.of(new Enrollment()));
        when(reviewProvider.findByAuthorIdAndChapterId(review.getAuthorId(), review.getChapterId())).thenReturn(Optional.empty());

        // WHEN
        reviewService.init(review);

        // THEN
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewProvider).save(argumentCaptor.capture());
        Review capturedReview = argumentCaptor.getValue();
        assertThat(capturedReview.getAuthorId()).isEqualTo(review.getAuthorId());
        assertThat(capturedReview.getChapterId()).isEqualTo(review.getChapterId());
        assertThat(capturedReview.getCourseId()).isEqualTo(courseId);
        assertThat(capturedReview.getChapterExplanations()).isEqualTo(review.getChapterExplanations());
        assertThat(capturedReview.getChapterSatisfactionScore()).isEqualTo(review.getChapterSatisfactionScore());
        assertThat(capturedReview.getChapterUnderstandingScore()).isEqualTo(review.getChapterUnderstandingScore());
        assertThat(capturedReview.isRecommendCourse()).isEqualTo(review.isRecommendCourse());
        assertThat(capturedReview.getOverallChapterSatisfaction()).isEqualTo(review.getOverallChapterSatisfaction());
    }

    @DisplayName("WHEN a user wants to init an existing review and has active enrollment, the review is modified.")
    @Test
    void should_SaveReview_whenInitReview_hasNonAdminUser_andHasActiveEnrollment() throws ResourceNotFoundException, ForbiddenActionException {
        // GIVEN
        User reviewer = ZerofiltreUtilsTest.createMockUser(false);

        review = new Review();
        review.setAuthorId(reviewer.getId());
        review.setChapterId(1L);
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);

        Review existingReview = new Review();
        existingReview.setAuthorId(reviewer.getId());
        existingReview.setChapterId(1L);
        existingReview.setChapterExplanations("Great chapter 1");
        existingReview.setChapterSatisfactionScore(4);
        existingReview.setChapterUnderstandingScore(4);
        existingReview.setRecommendCourse(false);
        existingReview.setOverallChapterSatisfaction(4);

        when(userProvider.userOfId(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(courseProvider.courseIdOfChapterId(review.getChapterId())).thenReturn(2L);
        when(enrollmentProvider.enrollmentOf(reviewer.getId(), 2L, true)).thenReturn(Optional.of(new Enrollment()));
        when(reviewProvider.findByAuthorIdAndChapterId(review.getAuthorId(), review.getChapterId())).thenReturn(Optional.of(existingReview));

        // WHEN
        reviewService.init(review);

        // THEN
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewProvider).save(argumentCaptor.capture());
        Review capturedReview = argumentCaptor.getValue();
        assertThat(capturedReview.getChapterExplanations()).isEqualTo(review.getChapterExplanations());
        assertThat(capturedReview.getChapterSatisfactionScore()).isEqualTo(review.getChapterSatisfactionScore());
        assertThat(capturedReview.getChapterUnderstandingScore()).isEqualTo(review.getChapterUnderstandingScore());
        assertThat(capturedReview.isRecommendCourse()).isEqualTo(review.isRecommendCourse());
        assertThat(capturedReview.getOverallChapterSatisfaction()).isEqualTo(review.getOverallChapterSatisfaction());
    }

    @DisplayName("WHEN a non-admin user wants to init a review and has no active enrollment, a forbidden action exception is thrown.")
    @Test
    void should_throws_ForbiddenActionException_whenInitReview_hasNonAdminUser_andNotEnrollment() {
        // GIVEN
        User reviewer = ZerofiltreUtilsTest.createMockUser(false);
        long courseIf = 2L;

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(reviewer));
        when(courseProvider.courseIdOfChapterId(anyLong())).thenReturn(courseIf);
        when(enrollmentProvider.enrollmentOf(reviewer.getId(), courseIf, true))
                .thenReturn(Optional.empty());

        // WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> reviewService.init(new Review()))
                .withMessage("User is not enrolled to this course, so you cannot review it");

    }

    @DisplayName("Find a review by id throws ResourceNotFoundException if review does not exist")
    @Test
    void find_by_id_throws_ResourceNotFoundException_if_review_does_not_exist() {
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reviewService.findById(1L));
    }

    @DisplayName("Find a review by id returns review when it exists")
    @Test
    void find_by_id_return_the_review_if_it_exists() throws ResourceNotFoundException {
        // GIVEN
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.of(new Review()));

        // WHEN
        reviewService.findById(12L);

        // THEN
        verify(reviewProvider).findById(anyLong());
    }

    @DisplayName("Update review throws ResourceNotFoundException when user does not exist")
    @Test
    void update_review_throws_ResourceNotFoundException_when_user_does_not_exist() {
        // GIVEN
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        // WHEN THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reviewService.update(4L, 12L, new Review()))
                .withMessage("User does not exist");

        verify(reviewProvider, never()).save(any());
    }

    @DisplayName("Update review throws ResourceNotFoundException when review does not exist")
    @Test
    void update_review_throws_ResourceNotFoundException_when_review_does_not_exist() {
        // GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);
        long reviewId = 4L;

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reviewService.update(reviewId, user.getId(), review))
                .withMessage("The review with id: " + reviewId + " does not exist");

        verify(reviewProvider, never()).save(any());
    }

    @DisplayName("WHEN a non-admin user is author of the review and not enrolled in a course wants update a review, THEN a forbidden action exception is thrown.")
    @Test
    void shouldThrowException_whenUpdateReview_asNonAdminUser_andAuthorOfReview_andNotEnrolledInCourse() {
        // GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.of(new Review()));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.empty());

        // WHEN THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> reviewService.update(4L, user.getId(), new Review()))
                .withMessage("You are not allowed to update this review");

        verify(reviewProvider, never()).save(any());
    }

    @DisplayName("WHEN a non-admin user who is not the author of the review wants to update the review, THEN a forbidden action exception is thrown.")
    @Test
    void shouldThrowException_whenUpdateReview_asNonAdminUser_andNotAuthorOfReview() {
        // GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);

        Review existingReview = new Review();
        existingReview.setAuthorId(user.getId() + 4);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.of(existingReview));

        // WHEN THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> reviewService.update(4L, user.getId(), new Review()))
                .withMessage("You are not allowed to update this review");

        verify(reviewProvider, never()).save(any());
    }

    @DisplayName("WHEN an admin user wants to update the review, THEN the review is updated.")
    @Test
    void shouldUpdatedReview_whenUpdateReview_asAdminUser() throws ForbiddenActionException, ResourceNotFoundException {
        // GIVEN
        User reviewer = ZerofiltreUtilsTest.createMockUser(true);

        review = new Review();
        review.setId(2L);
        review.setAuthorId(reviewer.getId());
        review.setChapterId(1L);
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);

        Review existingReview = new Review();
        existingReview.setId(review.getId());
        existingReview.setAuthorId(review.getId());
        existingReview.setChapterId(1L);
        existingReview.setChapterExplanations("Great chapter 1");
        existingReview.setChapterSatisfactionScore(4);
        existingReview.setChapterUnderstandingScore(4);
        existingReview.setRecommendCourse(false);
        existingReview.setOverallChapterSatisfaction(4);

        when(userProvider.userOfId(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(reviewProvider.findById(review.getId())).thenReturn(Optional.of(review));

        // WHEN
        reviewService.update(review.getId(), reviewer.getId(), review);

        // THEN
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewProvider).save(argumentCaptor.capture());
        Review capturedReview = argumentCaptor.getValue();
        assertThat(capturedReview.getChapterExplanations()).isEqualTo(review.getChapterExplanations());
        assertThat(capturedReview.getChapterSatisfactionScore()).isEqualTo(review.getChapterSatisfactionScore());
        assertThat(capturedReview.getChapterUnderstandingScore()).isEqualTo(review.getChapterUnderstandingScore());
        assertThat(capturedReview.isRecommendCourse()).isEqualTo(review.isRecommendCourse());
        assertThat(capturedReview.getOverallChapterSatisfaction()).isEqualTo(review.getOverallChapterSatisfaction());
    }

    @DisplayName("WHEN a non-admin user who is the author of the review and enrolled in a course wants to update the review, THEN the review is updated.")
    @Test
    void shouldUpdatedReview_whenUpdateReview_asNonAdminUser_andAuthorOfReview_andEnrolledInCourse() throws ForbiddenActionException, ResourceNotFoundException {
        // GIVEN
        User reviewer = ZerofiltreUtilsTest.createMockUser(false);

        review = new Review();
        review.setChapterExplanations("Great chapter");
        review.setChapterSatisfactionScore(5);
        review.setChapterUnderstandingScore(5);
        review.setRecommendCourse(true);
        review.setOverallChapterSatisfaction(5);

        Review existingReview = new Review();
        existingReview.setId(2L);
        existingReview.setAuthorId(reviewer.getId());
        existingReview.setChapterId(1L);
        existingReview.setChapterExplanations("Great chapter 1");
        existingReview.setChapterSatisfactionScore(4);
        existingReview.setChapterUnderstandingScore(4);
        existingReview.setRecommendCourse(false);
        existingReview.setOverallChapterSatisfaction(4);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(reviewer));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.of(existingReview));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(new Enrollment()));

        // WHEN
        reviewService.update(existingReview.getId(), reviewer.getId(), review);

        // THEN
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewProvider).save(argumentCaptor.capture());
        Review capturedReview = argumentCaptor.getValue();
        assertThat(capturedReview.getChapterExplanations()).isEqualTo(review.getChapterExplanations());
        assertThat(capturedReview.getChapterSatisfactionScore()).isEqualTo(review.getChapterSatisfactionScore());
        assertThat(capturedReview.getChapterUnderstandingScore()).isEqualTo(review.getChapterUnderstandingScore());
        assertThat(capturedReview.isRecommendCourse()).isEqualTo(review.isRecommendCourse());
        assertThat(capturedReview.getOverallChapterSatisfaction()).isEqualTo(review.getOverallChapterSatisfaction());
    }

    @DisplayName("Must update the existing review when the same user try to give another review on the same chapter")
    @Test
    void must_update_the_existing_review_when_the_same_user_try_give_another_review() throws ForbiddenActionException, ResourceNotFoundException {
        // GIVEN
        User reviewer = ZerofiltreUtilsTest.createMockUser(false);

        review = new Review();
        review.setId(2L);

        Review existingReview = new Review();
        existingReview.setId(review.getId());
        existingReview.setAuthorId(reviewer.getId());
        existingReview.setChapterId(1L);
        existingReview.setChapterExplanations("Great chapter 1");
        existingReview.setChapterSatisfactionScore(4);
        existingReview.setChapterUnderstandingScore(4);
        existingReview.setRecommendCourse(false);
        existingReview.setOverallChapterSatisfaction(4);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(reviewer));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.of(existingReview));
        when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), anyBoolean())).thenReturn(Optional.of(new Enrollment()));

        // WHEN
        reviewService.update(review.getId(), reviewer.getId(), review);

        // THEN
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewProvider).save(argumentCaptor.capture());
        Review capturedReview = argumentCaptor.getValue();
        assertThat(capturedReview.getChapterExplanations()).isEqualTo(review.getChapterExplanations());
        assertThat(capturedReview.getChapterSatisfactionScore()).isEqualTo(review.getChapterSatisfactionScore());
        assertThat(capturedReview.getChapterUnderstandingScore()).isEqualTo(review.getChapterUnderstandingScore());
        assertThat(capturedReview.isRecommendCourse()).isEqualTo(review.isRecommendCourse());
        assertThat(capturedReview.getOverallChapterSatisfaction()).isEqualTo(review.getOverallChapterSatisfaction());
    }

    @DisplayName("Delete review throws ResourceNotFoundException when user does not exist")
    @Test
    void delete_throws_ResourceNotFoundException_when_user_does_not_exist() {
        // GIVEN
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        // WHEN THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reviewService.delete(4L, 12L))
                .withMessage("User does not exist");

        verify(reviewProvider, never()).deleteById(anyLong());
    }

    @DisplayName("Delete review throws ResourceNotFoundException when review does not exist")
    @Test
    void delete_review_throws_ResourceNotFoundException_when_review_does_not_exist() {
        // GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(true);
        long reviewId = 4L;

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reviewService.delete(reviewId, user.getId()))
                .withMessage("The review with id: " + reviewId + " does not exist");

        verify(reviewProvider, never()).deleteById(anyLong());
    }

    @DisplayName("Delete review, you're not an admin throw a ForbiddenActionException")
    @Test
    void delete_review_throws_ForbiddenActionException_when_you_are_not_admin() {
        // GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        // WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> reviewService.delete(4L, user.getId()))
                .withMessage("You are not allowed to delete this review");

        verify(reviewProvider, never()).deleteById(anyLong());
    }

    @DisplayName("Delete review works if user is admin and review exists")
    @Test
    void delete_review_works_if_user_is_admin() throws ForbiddenActionException, ResourceNotFoundException {
        // GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(true);

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.of(new Review()));

        // WHEN
        reviewService.delete(4L, user.getId());

        // THEN
        verify(userProvider).userOfId(anyLong());
        verify(reviewProvider).findById(anyLong());
        verify(reviewProvider).deleteById(anyLong());
    }

}