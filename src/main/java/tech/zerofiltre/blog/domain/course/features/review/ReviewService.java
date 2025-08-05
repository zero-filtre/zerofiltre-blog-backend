package tech.zerofiltre.blog.domain.course.features.review;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

@RequiredArgsConstructor
public class ReviewService {

    private static final String USER_DOES_NOT_EXIST = "User does not exist";
    public static final String DOES_NOT_EXIST = " does not exist";
    public static final String THE_REVIEW_WITH_ID = "The review with id: ";

    private final ReviewProvider reviewProvider;
    private final UserProvider userProvider;
    private final EnrollmentProvider enrollmentProvider;
    private final CourseProvider courseProvider;

    public Review init(Review review) throws ResourceNotFoundException, ForbiddenActionException {
        Optional<User> reviewer = userProvider.userOfId(review.getAuthorId());
        if (reviewer.isEmpty())
            throw new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(review.getAuthorId()));

        long courseId = courseProvider.courseIdOfChapterId(review.getChapterId());
        boolean doesUserHasActiveEnrollment = userHasActiveEnrollmentInCourse(reviewer.get().getId(), courseId);
        if (doesUserHasActiveEnrollment) {
            Optional<Review> existingReview = reviewProvider.findByAuthorIdAndChapterId(review.getAuthorId(), review.getChapterId());
            if (existingReview.isEmpty()) {
                review.setCourseId(courseId);
                return reviewProvider.save(review);
            } else {
                Review updatedReview = updateExistingReview(existingReview.get(), review);
                return reviewProvider.save(updatedReview);
            }
        } else {
            throw new ForbiddenActionException("User is not enrolled to this course, so you cannot review it");
        }
    }

    public Review findById(long id) throws ResourceNotFoundException {
        return reviewProvider.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(THE_REVIEW_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));
    }

    public Review update(long reviewId, long userId, Review review) throws ForbiddenActionException, ResourceNotFoundException {
        User currentUser = userProvider.userOfId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(userId)));

        Optional<Review> existingReview = reviewProvider.findById(reviewId);
        if (existingReview.isEmpty())
            throw (new ResourceNotFoundException(THE_REVIEW_WITH_ID + reviewId + DOES_NOT_EXIST, String.valueOf(reviewId)));

        if (!currentUser.isAdmin()) {
            long courseId = courseProvider.courseIdOfChapterId(existingReview.get().getChapterId());
            boolean doesUserHasActiveEnrollment = userHasActiveEnrollmentInCourse(currentUser.getId(), courseId);
            boolean isNotAuthor = currentUser.getId() != existingReview.get().getAuthorId();
            if (isNotAuthor || !doesUserHasActiveEnrollment) {
                throw new ForbiddenActionException("You are not allowed to update this review");
            }
        }

        Review updatedReview = updateExistingReview(existingReview.get(), review);

        return reviewProvider.save(updatedReview);
    }

    public void delete(long reviewId, long userId) throws ForbiddenActionException, ResourceNotFoundException {
        User currentUser = userProvider.userOfId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(userId)));

        if (!currentUser.isAdmin())
            throw new ForbiddenActionException("You are not allowed to delete this review");

        Optional<Review> existingReview = reviewProvider.findById(reviewId);
        if (existingReview.isEmpty())
            throw (new ResourceNotFoundException(THE_REVIEW_WITH_ID + reviewId + DOES_NOT_EXIST, String.valueOf(reviewId)));

        reviewProvider.deleteById(reviewId);
    }

    private Review updateExistingReview(Review existingReview, Review review) {
        existingReview.setChapterExplanations(review.getChapterExplanations());
        existingReview.setChapterSatisfactionScore(review.getChapterSatisfactionScore());
        existingReview.setChapterUnderstandingScore(review.getChapterUnderstandingScore());
        existingReview.setRecommendCourse(review.isRecommendCourse());
        existingReview.setOverallChapterSatisfaction(review.getOverallChapterSatisfaction());
        existingReview.setImprovementSuggestion(review.getImprovementSuggestion());
        existingReview.setWhyRecommendingThisCourse(review.getWhyRecommendingThisCourse());
        existingReview.setFavoriteLearningToolOfTheChapter(review.getFavoriteLearningToolOfTheChapter());
        existingReview.setReasonFavoriteLearningToolOfTheChapter(review.getReasonFavoriteLearningToolOfTheChapter());

        return existingReview;
    }

    private boolean userHasActiveEnrollmentInCourse(long reviewerId, long courseId) {
        return enrollmentProvider.enrollmentOf(reviewerId, courseId, true).isPresent();
    }

}
