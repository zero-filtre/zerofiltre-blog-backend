package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyCourseService {

    private final CompanyCourseProvider companyCourseProvider;
    private final DataChecker checker;

    public LinkCompanyCourse link(User currentUser, long companyId, long courseId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminUser(currentUser);
        checker.companyExists(companyId);
        checker.courseExists(courseId);

        Optional<LinkCompanyCourse> existingCompanyCourse = companyCourseProvider.findByCompanyIdAndCourseId(companyId, courseId);
        if(existingCompanyCourse.isEmpty()) {
            LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(0, companyId, courseId, true, LocalDateTime.now(), null);
            return companyCourseProvider.save(linkCompanyCourse);
        } else if(!Objects.isNull(existingCompanyCourse.get().getSuspendedAt())) {
            existingCompanyCourse.get().setActive(true);
            existingCompanyCourse.get().setSuspendedAt(null);

            return companyCourseProvider.save(existingCompanyCourse.get());
        } else {
            return existingCompanyCourse.get();
        }
    }

    public void activeAllByCompanyId(User user, long companyId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminUser(user);
        checker.companyExists(companyId);

        for(LinkCompanyCourse c : companyCourseProvider.findAllByCompanyId(companyId)) {
            c.setActive(true);
            c.setSuspendedAt(null);
            companyCourseProvider.save(c);
        }
    }

    public Optional<LinkCompanyCourse> find(User user, long companyId, long courseId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminOrCompanyUser(user, companyId);
        checker.companyExists(companyId);
        checker.courseExists(courseId);

        return companyCourseProvider.findByCompanyIdAndCourseId(companyId, courseId);
    }

    public Page<LinkCompanyCourse> findAllByCompanyId(User user, int pageNumber, int pageSize, long companyId) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminOrCompanyUser(user, companyId);
        checker.companyExists(companyId);

        return companyCourseProvider.findAllByCompanyIdByPage(pageNumber, pageSize, companyId);
    }

    public long getLinkCompanyCourseIdIfCourseIsActive(long companyId, long courseId) throws ResourceNotFoundException {
        checker.companyCourseExists(companyId, courseId);

        return companyCourseProvider.findByCompanyIdAndCourseId(companyId, courseId, true).get().getId();
    }

    public void unlink(User user, long companyId, long courseId, boolean delete) throws ForbiddenActionException {
        checker.isAdminOrCompanyAdmin(user, companyId);

        if(delete) {
            Optional<LinkCompanyCourse> companyCourse = companyCourseProvider.findByCompanyIdAndCourseId(companyId, courseId);
            companyCourse.ifPresent(companyCourseProvider::delete);
        } else { // suspend
            Optional<LinkCompanyCourse> companyCourse = companyCourseProvider.findByCompanyIdAndCourseId(companyId, courseId, true);
            companyCourse.ifPresent(this::suspendLink);
        }
    }

    public void unlinkAllByCompanyId(User user, long companyId, boolean delete) throws ForbiddenActionException, ResourceNotFoundException {
        System.out.println("méthode: unLinkAllByCompanyId");
        checker.isAdminOrCompanyAdmin(user, companyId);
        checker.companyExists(companyId);

        if(delete) {
            companyCourseProvider.deleteAllByCompanyId(companyId);
        } else { // suspend
            for(LinkCompanyCourse c : companyCourseProvider.findAllByCompanyId(companyId)) {
                suspendLink(c);
            }
        }
    }

    public void unlinkAllByCourseId(User user, long courseId, boolean delete) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminUser(user);
        checker.courseExists(courseId);

        if(delete) {
            companyCourseProvider.deleteAllByCourseId(courseId);
        } else { // suspend
            for(LinkCompanyCourse c : companyCourseProvider.findAllByCourseId(courseId)) {
                suspendLink(c);
            }
        }
    }

    void suspendLink(LinkCompanyCourse linkCompanyCourse) {
        if(Objects.isNull(linkCompanyCourse.getSuspendedAt())) {
            linkCompanyCourse.setActive(false);
            linkCompanyCourse.setSuspendedAt(LocalDateTime.now());
            companyCourseProvider.save(linkCompanyCourse);
        }
    }

}
