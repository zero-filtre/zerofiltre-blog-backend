package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/company/course")
public class CompanyCourseController {

    private final SecurityContextManager securityContextManager;
    private final CompanyCourseService companyCourseService;
    private final MessageSource sources;

    public CompanyCourseController(SecurityContextManager securityContextManager, CompanyCourseService companyCourseService, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.companyCourseService = companyCourseService;
        this.sources = sources;
    }

    @PostMapping
    public void link(@RequestParam long companyId, @RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        companyCourseService.link(user, companyId, courseId);
    }

    @PostMapping("/activeAllCourses")
    public void activeAllCoursesByCompanyId(@RequestParam long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.activeAllByCompanyId(user, companyId);
    }

    @GetMapping
    public ResponseEntity<LinkCompanyCourse> find(@RequestParam long companyId, @RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Optional<LinkCompanyCourse> course = companyCourseService.find(user, companyId, courseId);

        return course.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

    }

    @GetMapping("/all")
    public Page<LinkCompanyCourse> findAllByCompanyId(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return companyCourseService.findAllByCompanyId(user, pageNumber, pageSize, companyId);
    }

    @DeleteMapping
    public void unLink(@RequestParam long companyId, @RequestParam long courseId, @RequestParam boolean delete) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.unlink(user, companyId, courseId, delete);
    }

    @DeleteMapping("/all")
    public void unlinkAll(@RequestParam(required = false) Long companyId, @RequestParam(required = false) Long courseId, @RequestParam boolean delete) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        if(!Objects.isNull(companyId)) {
            companyCourseService.unlinkAllByCompanyId(user, companyId.intValue(), delete);
        } else if(!Objects.isNull(courseId)) {
            companyCourseService.unlinkAllByCourseId(user, courseId.intValue(), delete);
        }
    }

}
