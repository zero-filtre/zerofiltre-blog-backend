package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.course.features.review.ReviewService;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.mapper.ReviewVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.ReviewVM;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewProvider reviewProvider;
    private final SecurityContextManager securityContextManager;
    private final ReviewVMMapper mapper = Mappers.getMapper(ReviewVMMapper.class);

    public ReviewController(ReviewProvider reviewProvider, UserProvider userProvider, EnrollmentProvider enrollmentProvider, CourseProvider courseProvider, SecurityContextManager securityContextManager) {
        this.reviewProvider = reviewProvider;
        this.securityContextManager = securityContextManager;
        this.reviewService = new ReviewService(reviewProvider, userProvider, enrollmentProvider, courseProvider);
    }

    @PostMapping
    public Review createOrUpdateReview(@RequestBody @Valid ReviewVM reviewVM) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Review review = mapper.fromVM(reviewVM);
        review.setAuthorId(user.getId());
        return reviewService.init(review);
    }

    @PatchMapping("/{id}")
    public Review updateReview(@PathVariable("id") long id, @RequestBody @Valid ReviewVM reviewVM) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return reviewService.update(id, user.getId(), mapper.fromVM(reviewVM));
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable long id) throws ResourceNotFoundException {
        return reviewService.findById(id);
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewProvider.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        reviewService.delete(id, user.getId());
    }
}

