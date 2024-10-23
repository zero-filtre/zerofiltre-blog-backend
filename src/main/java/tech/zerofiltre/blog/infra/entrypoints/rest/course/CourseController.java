package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.features.course.CourseService;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Section;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
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
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {

    private final SecurityContextManager securityContextManager;
    private final CourseService courseService;
    private final MessageSource sources;


    public CourseController(SecurityContextManager securityContextManager, CourseService courseService, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.courseService = courseService;
        this.sources = sources;
    }

    @GetMapping("/{id}")
    public Course courseById(@PathVariable("id") long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return the wanted course");
        }
        return courseService.findById(courseId, user);
    }

    @GetMapping("/{id}/{companyId}")
    public Course courseByIdAndCompanyId(@PathVariable("id") long courseId, @PathVariable long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = null;
        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (ZerofiltreException e) {
            log.debug("We did not find a connected user but we can still return the wanted course");
        }
        return courseService.findByIdAndCompanyId(courseId, user, companyId);
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
            log.debug("We did not find a connected user but we can still return published courses");
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
    public Course init(@RequestParam @NotNull @NotEmpty String title, @RequestParam(required = false) Long companyId) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        return courseService.init(title, user, Objects.isNull(companyId) ? 0 : companyId.intValue());
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
