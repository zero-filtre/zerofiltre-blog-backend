package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Section;
import tech.zerofiltre.blog.domain.course.use_cases.course.CourseService;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.PublishOrSaveCourseVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.SectionVM;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {

    private final SecurityContextManager securityContextManager;
    private final CourseService courseService;
    private final MessageSource sources;

    public CourseController(SecurityContextManager securityContextManager, CourseProvider courseProvider, TagProvider tagProvider, LoggerProvider loggerProvider, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.sources = sources;
        courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
    }

    @GetMapping("/{id}")
    public Course courseById(@PathVariable("id") long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return the wanted course", e);
        }
        return courseService.findById(courseId, user);
    }

    @PatchMapping
    public Course save(@RequestBody @Valid PublishOrSaveCourseVM publishOrSaveCourseVM) throws ZerofiltreException {
        return saveCourse(publishOrSaveCourseVM, Status.DRAFT);
    }


    @PatchMapping("/publish")
    public Course publish(@RequestBody @Valid PublishOrSaveCourseVM publishOrSaveCourseVM) throws ZerofiltreException {
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
        } catch (ZerofiltreException e) {
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
    public Course init(@RequestParam @NotNull @NotEmpty String title) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        return courseService.init(title, user);
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable("id") long courseId, HttpServletRequest request) throws ZerofiltreException {
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
