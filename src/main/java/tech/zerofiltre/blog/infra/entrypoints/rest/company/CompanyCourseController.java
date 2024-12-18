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

import java.util.Optional;

@RestController
//@RequestMapping("/company/course")
public class CompanyCourseController {

    private final SecurityContextManager securityContextManager;
    private final CompanyCourseService companyCourseService;
    private final MessageSource sources;

    public CompanyCourseController(SecurityContextManager securityContextManager, CompanyCourseService companyCourseService, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.companyCourseService = companyCourseService;
        this.sources = sources;
    }

    @PostMapping("/company/{companyId}/course/{courseId}")
    public void link(@PathVariable long companyId, @PathVariable long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        companyCourseService.link(user, companyId, courseId);
    }

    @PostMapping("/company/{companyId}/course/active")
    public void activeAllCoursesByCompanyId(@PathVariable long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.activeAllByCompanyId(user, companyId);
    }

    @GetMapping("/company/{companyId}/course/{courseId}")
    public ResponseEntity<LinkCompanyCourse> find(@PathVariable long companyId, @PathVariable long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Optional<LinkCompanyCourse> course = companyCourseService.find(user, companyId, courseId);

        return course.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

    }

    @GetMapping("/company/{companyId}/course")
    public Page<LinkCompanyCourse> findAllByCompanyId(@RequestParam int pageNumber, @RequestParam int pageSize, @PathVariable long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return companyCourseService.findAllByCompanyId(user, pageNumber, pageSize, companyId);
    }

    @DeleteMapping("/company/{companyId}/course/{courseId}")
    public void unLink(@PathVariable long companyId, @PathVariable long courseId, @RequestParam boolean hard) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.unlink(user, companyId, courseId, hard);
    }

    @DeleteMapping("/company/{companyId}/course/{courseId}/all")
    public void unlinkAll(@PathVariable long companyId, @PathVariable long courseId, @RequestParam boolean hard) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        if (companyId != 0) {
            companyCourseService.unlinkAllByCompanyId(user, companyId, hard);
        } else if (courseId != 0) {
            companyCourseService.unlinkAllByCourseId(user, courseId, hard);
        } else {
            throw new ForbiddenActionException("You must at least set a company id or a course id");
        }
    }

}
