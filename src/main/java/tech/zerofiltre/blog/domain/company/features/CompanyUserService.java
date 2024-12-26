package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.util.Optional;

@RequiredArgsConstructor
public class CompanyUserService {

    private final CompanyUserProvider companyUserProvider;
    private final DataChecker checker;

    public LinkCompanyUser link(User connectedUser, long companyId, long userId, LinkCompanyUser.Role role) throws ForbiddenActionException, ResourceNotFoundException {
        checker.hasPermission(connectedUser, isCompanyAdmin(connectedUser, companyId), role);
        checker.companyExists(companyId);
        checker.userExists(userId);

        return companyUserProvider.save(new LinkCompanyUser(companyId, userId, role));
    }

    public Optional<LinkCompanyUser> find(User connectedUser, long companyId, long userId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminOrCompanyAdmin(connectedUser, companyId);
        checker.companyExists(companyId);
        checker.userExists(userId);

        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId);
    }

    public Page<LinkCompanyUser> findAllByCompanyId(User connectedUser, int pageNumber, int pageSize, long companyId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminOrCompanyAdmin(connectedUser, companyId);
        checker.companyExists(companyId);

        return companyUserProvider.findAllByCompanyId(pageNumber, pageSize, companyId);
    }

    public void unlink(User connectedUser, long companyId, long userId) throws ForbiddenActionException {
        checker.isAdminOrCompanyAdmin(connectedUser, companyId);

        Optional<LinkCompanyUser> companyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, userId);

        if(companyUser.isPresent()) {
            checker.hasPermission(connectedUser, isCompanyAdmin(connectedUser, companyId), companyUser.get().getRole());
            companyUserProvider.delete(companyUser.get());
        }
    }

    public void unlinkAllByCompanyId(User connectedUser, long companyId) throws ResourceNotFoundException, ForbiddenActionException {
        checker.isAdminOrCompanyAdmin(connectedUser, companyId);
        checker.companyExists(companyId);

        if(connectedUser.isAdmin()) {
            companyUserProvider.deleteAllByCompanyId(companyId);
        } else if(isCompanyAdmin(connectedUser, companyId)) {
            companyUserProvider.deleteAllByCompanyIdExceptAdminRole(companyId);
        }
    }

    public void unlinkAllByUserId(User connectedUser, long userId) throws ForbiddenActionException {
        checker.isAdminUser(connectedUser);

        companyUserProvider.deleteAllByUserId(userId);
    }

    boolean isCompanyAdmin(User user, long companyId) {
        Optional<LinkCompanyUser> companyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, user.getId());

        return companyUser.map(value -> value.getRole().equals(LinkCompanyUser.Role.ADMIN)).orElse(false);
    }

}
