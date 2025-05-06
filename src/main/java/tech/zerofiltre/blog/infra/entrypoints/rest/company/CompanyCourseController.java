package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.util.DataChecker;

import javax.validation.constraints.Pattern;
import java.util.Optional;

@Slf4j
@RestController
public class CompanyCourseController {

    private final SecurityContextManager securityContextManager;
    private final CompanyCourseService companyCourseService;

    public CompanyCourseController(SecurityContextManager securityContextManager, CompanyCourseProvider companyCourseProvider, EnrollmentProvider enrollmentProvider, DataChecker checker) {
        this.securityContextManager = securityContextManager;
        this.companyCourseService = new CompanyCourseService(companyCourseProvider, enrollmentProvider, checker);
    }

    @PostMapping("/company/{companyId}/course/{courseId}")
    public void link(@PathVariable long companyId, @PathVariable long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        companyCourseService.link(user, companyId, courseId);
    }

    @PostMapping("/company/{companyId}/course/active")
    public void activeAllByCompanyId(@PathVariable long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.activeAllByCompanyId(user, companyId);
    }

    @GetMapping("/company/{companyId}/course/{courseId}")
    public ResponseEntity<LinkCompanyCourse> find(@PathVariable long companyId, @PathVariable long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Optional<LinkCompanyCourse> course = companyCourseService.find(user, companyId, courseId);

        return course.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/company/{companyId}/course/status/{status}")
    public Page<Course> findAllCoursesByCompanyId(@PathVariable long companyId, @PathVariable @Pattern(regexp = "DRAFT|PUBLISHED|ARCHIVED|IN_REVIEW") String status, @RequestParam int pageNumber, @RequestParam int pageSize) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return companyCourseService.findAllCoursesByCompanyId(user, pageNumber, pageSize, companyId, Status.valueOf(status));
    }

    @DeleteMapping("/company/{companyId}/course/{courseId}")
    public void unlink(@PathVariable long companyId, @PathVariable long courseId, @RequestParam boolean hard) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.unlink(user, companyId, courseId, hard);
    }

    @DeleteMapping("/company/{companyId}/course/all")
    public void unlinkAllByCompanyId(@PathVariable long companyId, @RequestParam boolean hard) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();

        if(companyId < 1){
            throw new ForbiddenActionException("You must at least set a company id");
        }

        companyCourseService.unlinkAllByCompanyId(user, companyId, hard);
    }

    @DeleteMapping("/company/course/{courseId}/all")
    public void unlinkAllByCourseId(@PathVariable long courseId, @RequestParam boolean hard) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();

        if(courseId < 1){
            throw new ForbiddenActionException("You must at least set a course id");
        }

        companyCourseService.unlinkAllByCourseId(user, courseId, hard);
    }

}
