package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.*;
import org.springframework.context.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;

import javax.servlet.http.*;
import javax.validation.*;
import javax.validation.constraints.*;

@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {


    private final SecurityContextManager securityContextManager;
    private final CourseProvider courseProvider;
    private final UserProvider userProvider;
    private Course course;
    private final MessageSource sources;

    public CourseController(SecurityContextManager securityContextManager, CourseProvider courseProvider, UserProvider userProvider, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.courseProvider = courseProvider;
        this.userProvider = userProvider;
        this.sources = sources;
    }

    @GetMapping("/{id}")
    public Course courseById(@PathVariable("id") long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        course = Course.builder()
                .userProvider(userProvider)
                .courseProvider(courseProvider).build();
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (BlogException e) {
            log.debug("We did not find a connected user but we can still return the wanted course", e);
        }
        return course.findById(courseId, user);
    }

    @PatchMapping
    public Course save(@RequestBody @Valid PublishOrSaveCourseVM publishOrSaveCourseVM) throws BlogException {
        return Course.builder().courseProvider(courseProvider)
                .userProvider(userProvider)
                .title(publishOrSaveCourseVM.getTitle())
                .subTitle(publishOrSaveCourseVM.getSubTitle())
                .summary(publishOrSaveCourseVM.getSummary())
                .video(publishOrSaveCourseVM.getVideo())
                .thumbnail(publishOrSaveCourseVM.getThumbnail())
                .status(Status.DRAFT)
                .sections(publishOrSaveCourseVM.getSections())
                .tags(publishOrSaveCourseVM.getTags())
                .build()
                .save(securityContextManager.getAuthenticatedUser().getId());
    }

    @PatchMapping("/publish")
    public Course publish(@RequestBody @Valid PublishOrSaveCourseVM publishOrSaveCourseVM) throws BlogException {
        return Course.builder().courseProvider(courseProvider)
                .userProvider(userProvider)
                .title(publishOrSaveCourseVM.getTitle())
                .subTitle(publishOrSaveCourseVM.getSubTitle())
                .summary(publishOrSaveCourseVM.getSummary())
                .video(publishOrSaveCourseVM.getVideo())
                .thumbnail(publishOrSaveCourseVM.getThumbnail())
                .status(Status.PUBLISHED)
                .sections(publishOrSaveCourseVM.getSections())
                .tags(publishOrSaveCourseVM.getTags())
                .build()
                .save(securityContextManager.getAuthenticatedUser().getId());
    }

    @GetMapping
    public Page<Course> courses(
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam String status,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String tag
    ) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = null;
        course = Course.builder().courseProvider(courseProvider).build();

        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (BlogException e) {
            log.debug("We did not find a connected user but we can still return published courses", e);
        }


        FinderRequest request = new FinderRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setUser(user);
        request.setTag(tag);
        if (filter != null) {
            filter = filter.toUpperCase();
            request.setFilter(FinderRequest.Filter.valueOf(filter));
        }
        if (status != null) {
            status = status.toUpperCase();
            request.setStatus(Status.valueOf(status));
        }
        return course.of(request);
    }


    @PostMapping
    public Course init(@RequestParam @NotNull @NotEmpty String title) throws BlogException {
        course = Course.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .build();
        User user = securityContextManager.getAuthenticatedUser();
        return course.init(title, user);
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable("id") long courseId, HttpServletRequest request) throws BlogException {
        course = Course.builder().courseProvider(courseProvider).build();
        User user = securityContextManager.getAuthenticatedUser();
        course.delete(courseId, user);
        return sources.getMessage("message.delete.article.success", null, request.getLocale());
    }
}
