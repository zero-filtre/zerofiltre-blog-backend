package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.mapstruct.factory.Mappers;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyService;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper.RegisterCompanyVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper.UpdateCompanyVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.RegisterCompanyVM;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.UpdateCompanyVM;
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
    private final UpdateCompanyVMMapper updateCompanyVMMapper = Mappers.getMapper(UpdateCompanyVMMapper.class);
    private final MessageSource sources;

    public CompanyController(SecurityContextManager securityContextManager, CompanyProvider companyProvider, CompanyUserProvider companyUserProvider, CompanyCourseProvider companyCourseProvider, DataChecker checker, MessageSource sources) {
        this.securityContextManager = securityContextManager;
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
    public Company patch(@RequestBody @Valid UpdateCompanyVM updateCompanyVM) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        Company company = updateCompanyVMMapper.fromVM(updateCompanyVM);

        return companyService.patch(user, company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> findById(@PathVariable("id") long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Optional<Company> company = companyService.findById(user, companyId);

        return company.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

    }

    @GetMapping("/all")
    public Page<Company> findAll(@RequestParam int pageNumber, @RequestParam int pageSize, @RequestParam(required = false) Long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        return companyService.findAll(pageNumber, pageSize, user, null == userId ? 0 : userId);
    }

    @DeleteMapping
    public String delete(@RequestBody @Valid UpdateCompanyVM updateCompanyVM, HttpServletRequest request) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Company company = updateCompanyVMMapper.fromVM(updateCompanyVM);
        companyService.delete(user, company);

        return sources.getMessage("message.delete.company.success", null, request.getLocale());
    }

}
