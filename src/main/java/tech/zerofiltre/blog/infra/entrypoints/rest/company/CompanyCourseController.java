package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.company.features.IsAdminOrCompanyAdmin;
import tech.zerofiltre.blog.domain.company.features.IsAdminOrCompanyUser;
import tech.zerofiltre.blog.domain.company.features.IsCompanyExists;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.features.course.IsCourseExists;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.IsAdminUser;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import java.util.Optional;

@RestController
@RequestMapping("/company/course")
public class CompanyCourseController {

    private final SecurityContextManager securityContextManager;
    private final CompanyCourseService companyCourseService;
    private final MessageSource sources;

    public CompanyCourseController(UserProvider userProvider, SecurityContextManager securityContextManager, CompanyCourseProvider companyCourseProvider, CompanyProvider companyProvider, CourseProvider courseProvider, CompanyUserProvider companyUserProvider, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        IsAdminUser isAdminUser = new IsAdminUser();
        IsAdminOrCompanyAdmin isAdminOrCompanyAdmin = new IsAdminOrCompanyAdmin(companyUserProvider);
        IsAdminOrCompanyUser isAdminOrCompanyUser = new IsAdminOrCompanyUser(companyUserProvider);
        IsCompanyExists isCompanyExists = new IsCompanyExists(companyProvider);
        IsCourseExists isCourseExists = new IsCourseExists(courseProvider);
        this.companyCourseService = new CompanyCourseService(companyCourseProvider, isAdminUser, isAdminOrCompanyAdmin, isAdminOrCompanyUser, isCompanyExists, isCourseExists);
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
        companyCourseService.unLink(user, companyId, courseId, delete);
    }

    @DeleteMapping("/allByCompanyId")
    public void unLinkAllByCompanyId(@RequestParam long companyId, @RequestParam boolean delete) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.unLinkAllByCompanyId(user, companyId, delete);
    }

    @DeleteMapping("/allByCourseId")
    public void unlinkAllByCourseId(@RequestParam long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyCourseService.unlinkAllByCourseId(user, courseId);
    }

}
