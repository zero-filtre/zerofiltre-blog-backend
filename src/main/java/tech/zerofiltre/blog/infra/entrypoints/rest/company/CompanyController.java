package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.company.features.CompanyService;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
public class CompanyController {

    private final SecurityContextManager securityContextManager;
    private final CompanyService companyService;

    @PostMapping("/{companyId}/course")
    public Course addCourse(@RequestBody Course course, @PathVariable long companyId) throws UserNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return companyService.addCourse(companyId, user, course);
    }
}
