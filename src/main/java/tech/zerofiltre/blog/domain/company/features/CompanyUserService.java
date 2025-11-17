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
        checker.checkIfAdminOrCompanyAdmin(connectedUser, companyId);
        checker.checkCompanyExistence(companyId);
        checker.checkUserExistence(userId);

        Optional<LinkCompanyUser> existingCompanyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, userId);

        if(existingCompanyUser.isEmpty()) {
            LinkCompanyUser linkCompanyUser = new LinkCompanyUser(0, companyId, userId, role, true, LocalDateTime.now(), null);
            return companyUserProvider.save(linkCompanyUser);
        } else if(null != existingCompanyUser.get().getSuspendedAt()) {
            existingCompanyUser.get().setActive(true);
            existingCompanyUser.get().setRole(role);
            existingCompanyUser.get().setSuspendedAt(null);

            return companyUserProvider.save(existingCompanyUser.get());
        } else {
            if(role.equals(existingCompanyUser.get().getRole())) return existingCompanyUser.get();

            existingCompanyUser.get().setRole(role);
            return companyUserProvider.save(existingCompanyUser.get());
        }
    }

    public Optional<LinkCompanyUser> find(User connectedUser, long companyId, long userId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.checkIfAdminOrCompanyAdmin(connectedUser, companyId);
        checker.checkCompanyExistence(companyId);
        checker.checkUserExistence(userId);

        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId);
    }

    public Page<LinkCompanyUser> findAllByCompanyId(User connectedUser, int pageNumber, int pageSize, long companyId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.checkIfAdminOrCompanyAdmin(connectedUser, companyId);
        checker.checkCompanyExistence(companyId);

        return companyUserProvider.findAllByCompanyId(pageNumber, pageSize, companyId);
    }

    public long getLinkCompanyUserIdIfUserIsActive(long companyId, long userId) throws ResourceNotFoundException {
        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId, true).map(LinkCompanyUser::getId).orElseThrow(() -> new ResourceNotFoundException("We could not find the link between the company and the user.", ""));
    }

    public void unlink(User connectedUser, long companyId, long userId, boolean hard) throws ZerofiltreException {
        checker.checkIfAdminOrCompanyAdmin(connectedUser, companyId);

        Optional<LinkCompanyUser> companyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, userId);

        if(hard && companyUser.isPresent()) {
                companyUserProvider.delete(companyUser.get());
                suspendEnrollments(companyUser.get().getId());
                return;
        }

        if(!hard && companyUser.isPresent()) suspendLink(companyUser.get());
    }

    public void unlinkAllByCompanyId(User connectedUser, long companyId, boolean hard) throws ZerofiltreException {
        checker.checkIfAdminOrCompanyAdmin(connectedUser, companyId);
        checker.checkCompanyExistence(companyId);

        if(hard && connectedUser.isAdmin()) {
            for(LinkCompanyUser c : companyUserProvider.findAllByCompanyId(companyId)) {
                companyUserProvider.delete(c);
                suspendEnrollments(c.getId());
            }
            return;
        }

        if(hard && !connectedUser.isAdmin()) {
            for(LinkCompanyUser c : companyUserProvider.findAllByCompanyIdExceptAdminRole(companyId)) {
                companyUserProvider.delete(c);
                suspendEnrollments(c.getId());
            }
            return;
        }

        if(connectedUser.isAdmin()) {
            for(LinkCompanyUser c : companyUserProvider.findAllByCompanyId(companyId)) {
                suspendLink(c);
            }
            return;
        }

        for (LinkCompanyUser c : companyUserProvider.findAllByCompanyIdExceptAdminRole(companyId)) {
            suspendLink(c);
        }
    }

    public void unlinkAllByUserId(User connectedUser, long userId) throws ForbiddenActionException {
        checker.checkIfAdminUser(connectedUser);

        companyUserProvider.deleteAllByUserId(userId);
    }

    void suspendLink(LinkCompanyUser linkCompanyUser) throws ZerofiltreException {
        if(null == linkCompanyUser.getSuspendedAt()) {
            linkCompanyUser.setActive(false);
            linkCompanyUser.setSuspendedAt(LocalDateTime.now());
            companyUserProvider.save(linkCompanyUser);
        }
        suspendEnrollments(linkCompanyUser.getId());
    }

    void suspendEnrollments(long companyUserId) throws ZerofiltreException {
        for(Enrollment e : enrollmentProvider.findAllByCompanyUserId(companyUserId, true)) {
            e.setActive(false);
            e.setSuspendedAt(LocalDateTime.now());
            enrollmentProvider.save(e);
        }
    }
}
