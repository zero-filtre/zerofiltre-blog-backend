package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
public class CompanyUserService {

    private final CompanyUserProvider companyUserProvider;
    private final EnrollmentProvider enrollmentProvider;
    private final DataChecker checker;

    public LinkCompanyUser link(User connectedUser, long companyId, long userId, LinkCompanyUser.Role role) throws ForbiddenActionException, ResourceNotFoundException {
        checker.hasPermission(connectedUser, isCompanyAdmin(connectedUser, companyId), role);
        checker.companyExists(companyId);
        checker.userExists(userId);

        Optional<LinkCompanyUser> existingCompanyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, userId);

        if(existingCompanyUser.isEmpty()) {
            LinkCompanyUser linkCompanyUser = new LinkCompanyUser(0, companyId, userId, role, true, LocalDateTime.now(), null);
            return companyUserProvider.save(linkCompanyUser);
        } else if(null != existingCompanyUser.get().getSuspendedAt()) {
            existingCompanyUser.get().setActive(true);
            existingCompanyUser.get().setSuspendedAt(null);

            return companyUserProvider.save(existingCompanyUser.get());
        } else {
            return existingCompanyUser.get();
        }
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

    public long getLinkCompanyUserIdIfUserIsActive(long companyId, long userId) throws ResourceNotFoundException {
        checker.companyUserExists(companyId, userId);
        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId, true).map(LinkCompanyUser::getId).orElse(0L);
    }

    public void unlink(User connectedUser, long companyId, long userId) throws ZerofiltreException {
        checker.isAdminOrCompanyAdmin(connectedUser, companyId);

        Optional<LinkCompanyUser> companyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, userId);

        if(companyUser.isPresent()) {
            checker.hasPermission(connectedUser, isCompanyAdmin(connectedUser, companyId), companyUser.get().getRole());
            suspendLink(companyUser.get());
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

    void suspendLink(LinkCompanyUser linkCompanyUser) throws ZerofiltreException {
        if(null == linkCompanyUser.getSuspendedAt()) {
            linkCompanyUser.setActive(false);
            linkCompanyUser.setSuspendedAt(LocalDateTime.now());
            companyUserProvider.save(linkCompanyUser);
        }
        suspendEnrollment(linkCompanyUser.getId());
    }

    void suspendEnrollment(long companyUserId) throws ZerofiltreException {
        for(Enrollment e : enrollmentProvider.findAllByCompanyUserId(companyUserId, true)) {
            e.setActive(false);
            e.setSuspendedAt(LocalDateTime.now());
            enrollmentProvider.save(e);
        }
    }
}
