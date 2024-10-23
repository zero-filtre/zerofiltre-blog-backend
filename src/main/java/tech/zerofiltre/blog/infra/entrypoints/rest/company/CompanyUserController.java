package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.features.CompanyUserService;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import javax.validation.constraints.Pattern;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/company/user")
public class CompanyUserController {

    private final SecurityContextManager securityContextManager;
    private final CompanyUserService companyUserService;
    private final MessageSource sources;

    public CompanyUserController(SecurityContextManager securityContextManager, CompanyUserService companyUserService, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        this.companyUserService = companyUserService;
        this.sources = sources;
    }

    @PostMapping
    public void link(@RequestParam long companyId, @RequestParam long userId, @RequestParam @Pattern(regexp = "ROLE_COMPANY_ADMIN|ROLE_COMPANY_EDITOR|ROLE_COMPANY_VIEWER") String role) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        companyUserService.link(user, companyId, userId, LinkCompanyUser.Role.valueOf(role));
    }

    @GetMapping
    public ResponseEntity<LinkCompanyUser> find(@RequestParam long companyId, @RequestParam long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Optional<LinkCompanyUser> userLinked = companyUserService.find(user, companyId, userId);

        return userLinked.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/all")
    public Page<LinkCompanyUser> findAllByCompanyId(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return companyUserService.findAllByCompanyId(user, pageNumber, pageSize, companyId);
    }

    @DeleteMapping
    public void unlink(@RequestParam long companyId, @RequestParam long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        companyUserService.unlink(user, companyId, userId);
    }

    @DeleteMapping("/all")
    public void unLinkAll(@RequestParam(required = false) Long companyId, @RequestParam(required = false) Long courseId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();

        if(!Objects.isNull(companyId)) {
            companyUserService.unlinkAllByCompanyId(user, companyId.intValue());
        } else if(!Objects.isNull(courseId)) {
            companyUserService.unlinkAllByUserId(user, courseId.intValue());
        }
    }

}
