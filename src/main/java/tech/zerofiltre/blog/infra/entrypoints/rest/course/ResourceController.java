package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.course.use_cases.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;

import javax.validation.*;

@Slf4j
@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final SecurityContextManager securityContextManager;


    public ResourceController(ChapterProvider chapterProvider, LessonProvider lessonProvider, ResourceProvider resourceProvider, CourseProvider courseProvider, SecurityContextManager securityContextManager) {
        this.securityContextManager = securityContextManager;
        this.resourceService = new ResourceService(resourceProvider, lessonProvider, chapterProvider, courseProvider);
    }

    @PostMapping
    public Resource createResource(@RequestBody @Valid ResourceVM resourceVM) throws ResourceNotFoundException, ForbiddenActionException {
        Resource resource = Resource.builder()
                .type(resourceVM.getType())
                .name(resourceVM.getName())
                .url(resourceVM.getUrl())
                .lessonId(resourceVM.getLessonId())
                .build();
        return resourceService.createResource(resource, securityContextManager.getAuthenticatedUser());
    }

    @DeleteMapping("/{resourceId}")
    public void deleteResource(@PathVariable long resourceId) throws ResourceNotFoundException, ForbiddenActionException {
        resourceService.deleteResource(resourceId, securityContextManager.getAuthenticatedUser());
    }

}
