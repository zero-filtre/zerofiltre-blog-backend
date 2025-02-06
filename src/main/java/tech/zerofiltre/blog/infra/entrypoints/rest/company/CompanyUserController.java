package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyUserService;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.util.DataChecker;

import javax.validation.constraints.Pattern;
import java.util.Optional;

@RestController
public class CompanyUserController {

    private final SecurityContextManager securityContextManager;
    private final CompanyUserService companyUserService;
    private final MessageSource sources;

    public CompanyUserController(SecurityContextManager securityContextManager, MessageSource sources, CompanyUserProvider companyUserProvider, EnrollmentProvider enrollmentProvider, DataChecker checker) {
        this.securityContextManager = securityContextManager;
        this.companyUserService = new CompanyUserService(companyUserProvider, enrollmentProvider, checker);
        this.sources = sources;
    }

    @PostMapping("/company/{companyId}/user/{userId}/role/{role}")
    public void link(@PathVariable long companyId, @PathVariable long userId, @PathVariable @Pattern(regexp = "ADMIN|EDITOR|VIEWER") String role) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        companyUserService.link(user, companyId, userId, LinkCompanyUser.Role.valueOf(role));
    }

    @GetMapping("/company/{companyId}/user/{userId}")
    public ResponseEntity<LinkCompanyUser> find(@PathVariable long companyId, @PathVariable long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Optional<LinkCompanyUser> userLinked = companyUserService.find(user, companyId, userId);

        return userLinked.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/company/{companyId}/user")
    public Page<LinkCompanyUser> findAllByCompanyId(@PathVariable long companyId, @RequestParam int pageNumber, @RequestParam int pageSize) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return companyUserService.findAllByCompanyId(user, pageNumber, pageSize, companyId);
    }

    @DeleteMapping("/company/{companyId}/user/{userId}")
    public void unlink(@PathVariable long companyId, @PathVariable long userId, @RequestParam boolean hard) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        companyUserService.unlink(user, companyId, userId, hard);
    }

    @DeleteMapping("/company/{companyId}/user/all")
    public void unlinkAllByCompanyId(@PathVariable long companyId, @RequestParam boolean hard) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();

        if(companyId < 1){
            throw new ForbiddenActionException("You must at least set a company id");
        }

        companyUserService.unlinkAllByCompanyId(user, companyId, hard);
    }

    @DeleteMapping("/company/user/{userId}/all")
    public void unlinkAllByUserId(@PathVariable long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        if(userId < 1){
            throw new ForbiddenActionException("You must at least set a user id");
        }

        companyUserService.unlinkAllByUserId(user, userId);
    }

}
