package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.course.use_cases.enrollment.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;

@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {

    private final SecurityContextManager securityContextManager;
    private final Enroll enroll;
    private final Suspend suspend;
    private final CompleteLesson completeLesson;
    private final FindEnrollment findEnrollment;


    public EnrollmentController(
            EnrollmentProvider enrollmentProvider,
            CourseProvider courseProvider,
            UserProvider userProvider,
            SecurityContextManager securityContextManager,
            LessonProvider lessonProvider,
            ChapterProvider chapterProvider) {
        this.securityContextManager = securityContextManager;
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider);
        suspend = new Suspend(enrollmentProvider, courseProvider, chapterProvider);
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);
        findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
    }


    @PostMapping
    public Enrollment enroll(@RequestParam long courseId) throws ZerofiltreException {
        return enroll.execute(securityContextManager.getAuthenticatedUser().getId(), courseId, true);

    }

    @DeleteMapping
    public void unEnroll(@RequestParam long courseId) throws ZerofiltreException {
        suspend.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);
    }

    @PatchMapping("/complete")
    public Enrollment completeLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ZerofiltreException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), true);
    }

    @PatchMapping("/uncomplete")
    public Enrollment unCompleteLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ZerofiltreException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), false);
    }


    @GetMapping
    public Enrollment getEnrollment(@RequestParam long courseId, @RequestParam long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User executor = securityContextManager.getAuthenticatedUser();
        return findEnrollment.of(courseId, userId, executor);
    }

    @GetMapping("/user")
    Page<Course> coursesOfEnrollment(
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
        return findEnrollment.of(request);
    }
}
