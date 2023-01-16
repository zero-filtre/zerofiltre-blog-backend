package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.*;

import javax.validation.*;

@RequestMapping("/section")
@RestController
public class SectionController {

    private final SectionProvider sectionProvider;
    private final CourseProvider courseProvider;
    private final UserProvider userProvider;
    private final SecurityContextManager securityContextManager;

    private User user;

    public SectionController(SectionProvider sectionProvider, CourseProvider courseProvider, UserProvider userProvider, SecurityContextManager securityContextManager) {
        this.sectionProvider = sectionProvider;
        this.courseProvider = courseProvider;
        this.userProvider = userProvider;
        this.securityContextManager = securityContextManager;
    }

    @GetMapping("{id}")
    public Section sectionOfId(@PathVariable("id") long id) throws ResourceNotFoundException {
        return Section.builder().sectionProvider(sectionProvider).build().findById(id);
    }

    @PostMapping
    public Section createSection(@RequestBody @Valid SectionVM sectionVM) {
        return Section.builder()
                .sectionProvider(sectionProvider)
                .courseId(sectionVM.getCourseId())
                .content(sectionVM.getContent())
                .image(sectionVM.getImage())
                .position(sectionVM.getPosition())
                .title(sectionVM.getTitle())
                .build()
                .save();
    }

    @PatchMapping
    public Section updateSection(@RequestBody @Valid SectionVM sectionVM) throws ResourceNotFoundException {
        return Section.builder()
                .sectionProvider(sectionProvider)
                .build()
                .update(sectionVM.getId(), sectionVM.getTitle(), sectionVM.getContent(), sectionVM.getImage());
    }

    @DeleteMapping("{id}")
    public void deleteSection(@PathVariable("id") long id) throws ResourceNotFoundException, ForbiddenActionException {
        user = securityContextManager.getAuthenticatedUser();
        Section.builder().sectionProvider(sectionProvider)
                .id(id)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .build()
                .delete(user);
    }
}
