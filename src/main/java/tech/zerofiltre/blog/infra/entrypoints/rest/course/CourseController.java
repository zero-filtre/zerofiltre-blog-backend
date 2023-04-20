package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.*;
import org.springframework.context.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.course.use_cases.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;

import javax.servlet.http.*;
import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {

    private final SecurityContextManager securityContextManager;
    private final CourseService courseService;
    private final MessageSource sources;

    public CourseController(SecurityContextManager securityContextManager, CourseProvider courseProvider, TagProvider tagProvider, LoggerProvider loggerProvider, ChapterProvider chapterProvider, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.sources = sources;
        courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
    }

    @GetMapping("/{id}")
    public Course courseById(@PathVariable("id") long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (BlogException e) {
            log.debug("We did not find a connected user but we can still return the wanted course", e);
        }
        return courseService.findById(courseId, user);
    }

    @PatchMapping
    public Course save(@RequestBody @Valid PublishOrSaveCourseVM publishOrSaveCourseVM) throws BlogException {
        return saveCourse(publishOrSaveCourseVM, Status.DRAFT);
    }


    @PatchMapping("/publish")
    public Course publish(@RequestBody @Valid PublishOrSaveCourseVM publishOrSaveCourseVM) throws BlogException {
        return saveCourse(publishOrSaveCourseVM, Status.PUBLISHED);
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
        return courseService.of(request);
    }


    @PostMapping
    public Course init(@RequestParam @NotNull @NotEmpty String title) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        return courseService.init(title, user);
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable("id") long courseId, HttpServletRequest request) throws BlogException {
        User user = securityContextManager.getAuthenticatedUser();
        courseService.delete(courseId, user);
        return sources.getMessage("message.delete.course.success", null, request.getLocale());
    }

    private Course saveCourse(PublishOrSaveCourseVM publishOrSaveCourseVM, Status published) throws ResourceNotFoundException, ForbiddenActionException {
        Course course = new Course();
        course.setId(publishOrSaveCourseVM.getId());
        course.setTitle(publishOrSaveCourseVM.getTitle());
        course.setSubTitle(publishOrSaveCourseVM.getSubTitle());
        course.setSummary(publishOrSaveCourseVM.getSummary());
        course.setVideo(publishOrSaveCourseVM.getVideo());
        course.setThumbnail(publishOrSaveCourseVM.getThumbnail());
        course.setStatus(published);
        course.setSections(fromVMs(publishOrSaveCourseVM.getSections()));
        course.setTags(publishOrSaveCourseVM.getTags());
        return courseService.save(course, securityContextManager.getAuthenticatedUser());
    }


    private List<Section> fromVMs(List<SectionVM> sections) {
        List<Section> sectionList = new ArrayList<>();
        for (SectionVM sectionVM : sections) {
            sectionList.add(Section.builder()
                    .id(sectionVM.getId())
                    .position(sectionVM.getPosition())
                    .image(sectionVM.getImage())
                    .title(sectionVM.getTitle())
                    .content(sectionVM.getContent())
                    .build());
        }
        return sectionList;
    }
}
