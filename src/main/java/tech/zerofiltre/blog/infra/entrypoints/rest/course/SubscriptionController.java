package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.course.use_cases.subscription.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SecurityContextManager securityContextManager;
    private final Subscribe subscribe;
    private final Suspend suspend;
    private final CompleteLesson completeLesson;
    private final FindSubscription findSubscription;


    public SubscriptionController(
            SubscriptionProvider subscriptionProvider,
            CourseProvider courseProvider,
            UserProvider userProvider,
            SecurityContextManager securityContextManager,
            LessonProvider lessonProvider,
            ChapterProvider chapterProvider) {
        this.securityContextManager = securityContextManager;
        subscribe = new Subscribe(subscriptionProvider, courseProvider, userProvider);
        suspend = new Suspend(subscriptionProvider);
        completeLesson = new CompleteLesson(subscriptionProvider, lessonProvider, chapterProvider);
        findSubscription = new FindSubscription(subscriptionProvider);
    }


    @PostMapping
    public Subscription subscribe(@RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        return subscribe.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);

    }

    @DeleteMapping
    public void unsubscribe(@RequestParam long courseId) throws UserNotFoundException, ForbiddenActionException {
        suspend.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);
    }

    @PatchMapping("/complete")
    public Subscription completeLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), true);
    }

    @PatchMapping("/uncomplete")
    public Subscription unCompleteLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), false);
    }


    @GetMapping
    public Subscription getSubscription(@RequestParam long courseId, @RequestParam long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User executor = securityContextManager.getAuthenticatedUser();
        return findSubscription.of(courseId, userId, executor);
    }

    @GetMapping("/user")
    Page<Course> coursesOfSubscriptions(
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String tag) throws UserNotFoundException {
        FinderRequest request = new FinderRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setUser(securityContextManager.getAuthenticatedUser());
        request.setTag(tag);
        if (filter != null) {
            filter = filter.toUpperCase();
            request.setFilter(FinderRequest.Filter.valueOf(filter));
        }
        if (status != null) {
            status = status.toUpperCase();
            request.setStatus(Status.valueOf(status));
        }
        return findSubscription.of(request);
    }
}
