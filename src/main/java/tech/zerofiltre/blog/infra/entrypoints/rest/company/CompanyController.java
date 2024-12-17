package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.mapstruct.factory.Mappers;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyService;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper.CompanyVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper.RegisterCompanyVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.CompanyVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.RegisterCompanyVM;
import tech.zerofiltre.blog.util.DataChecker;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/company")
public class CompanyController {

    private final SecurityContextManager securityContextManager;
    private final CompanyService companyService;
    private final RegisterCompanyVMMapper registerCompanyVMMapper = Mappers.getMapper(RegisterCompanyVMMapper.class);
    private final CompanyVMMapper companyVMMapper = Mappers.getMapper(CompanyVMMapper.class);
    private final MessageSource sources;

    public CompanyController(UserProvider userProvider, SecurityContextManager securityContextManager, CourseProvider courseProvider, CompanyProvider companyProvider, CompanyUserProvider companyUserProvider, CompanyCourseProvider companyCourseProvider, MessageSource sources) {
        this.securityContextManager = securityContextManager;
        DataChecker checker = new DataChecker(userProvider, courseProvider, companyProvider, companyUserProvider, companyCourseProvider);
        this.companyService = new CompanyService(companyProvider, companyUserProvider, companyCourseProvider, checker);
        this.sources = sources;
    }

    @PostMapping
    public Company save(@RequestBody @Valid RegisterCompanyVM registerCompanyVM) throws UserNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Company company = registerCompanyVMMapper.fromVM(registerCompanyVM);

        return companyService.save(user, company);
    }

    @PatchMapping
    public Company patch(@RequestBody @Valid CompanyVM companyVM) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        Company company = companyVMMapper.fromVM(companyVM);

        return companyService.patch(user, company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getById(@PathVariable("id") long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Optional<Company> company = companyService.findById(user, companyId);

        return company.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

    }

    @DeleteMapping
    public String delete(@RequestBody @Valid CompanyVM companyVM, HttpServletRequest request) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Company company = companyVMMapper.fromVM(companyVM);
        companyService.delete(user, company);

        return sources.getMessage("message.delete.company.success", null, request.getLocale());
    }

}
