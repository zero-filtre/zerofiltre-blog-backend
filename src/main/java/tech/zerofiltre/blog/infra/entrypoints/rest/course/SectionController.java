package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;

import javax.validation.*;

@RequestMapping("/section")
@RestController
public class SectionController {

    private final SectionProvider sectionProvider;
    private final CourseProvider courseProvider;
    private final UserProvider userProvider;
    private final ChapterProvider chapterProvider;
    private final SecurityContextManager securityContextManager;
    private final LoggerProvider loggerProvider;

    public SectionController(SectionProvider sectionProvider, CourseProvider courseProvider, UserProvider userProvider, ChapterProvider chapterProvider, SecurityContextManager securityContextManager, LoggerProvider loggerProvider) {
        this.sectionProvider = sectionProvider;
        this.courseProvider = courseProvider;
        this.userProvider = userProvider;
        this.chapterProvider = chapterProvider;
        this.securityContextManager = securityContextManager;
        this.loggerProvider = loggerProvider;
    }

    @GetMapping("{id}")
    public Section sectionOfId(@PathVariable("id") long id) throws ResourceNotFoundException {
        return Section.builder().sectionProvider(sectionProvider).build().findById(id);
    }

    @PostMapping
    public Section createSection(@RequestBody @Valid SectionVM sectionVM) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return Section.builder()
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .courseId(sectionVM.getCourseId())
                .content(sectionVM.getContent())
                .image(sectionVM.getImage())
                .position(sectionVM.getPosition())
                .title(sectionVM.getTitle())
                .build()
                .init(user);
    }

    @PatchMapping
    public Section updateSection(@RequestBody @Valid SectionVM sectionVM) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        return Section.builder()
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .build()
                .update(sectionVM.getId(), sectionVM.getTitle(), sectionVM.getContent(), sectionVM.getImage(), sectionVM.getPosition(), user);
    }

    @DeleteMapping("{id}")
    public void deleteSection(@PathVariable("id") long id) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Section.builder().sectionProvider(sectionProvider)
                .id(id)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .chapterProvider(chapterProvider)
                .loggerProvider(loggerProvider)
                .build()
                .delete(user);
    }
}
