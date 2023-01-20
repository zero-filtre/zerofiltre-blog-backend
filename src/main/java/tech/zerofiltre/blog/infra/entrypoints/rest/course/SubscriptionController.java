package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.course.use_cases.subscription.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SecurityContextManager securityContextManager;
    private final Subscribe subscribe;
    private final Suspend suspend;
    private final CompleteLesson completeLesson;


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
    }


    @PostMapping
    public Subscription subscribe(@RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        return subscribe.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);

    }

    @DeleteMapping
    public void unsubscribe(@RequestParam long courseId) throws UserNotFoundException, ForbiddenActionException {
        suspend.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);
    }

    @PostMapping("/complete")
    public void completeLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId());
    }

}
