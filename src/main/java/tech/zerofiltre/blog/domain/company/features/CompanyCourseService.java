package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.features.course.IsCourseExists;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.features.IsAdminUser;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyCourseService {

    private final CompanyCourseProvider companyCourseProvider;
    private final IsAdminUser isAdminUser;
    private final IsAdminOrCompanyAdmin isAdminOrCompanyAdmin;
    private final IsAdminOrCompanyUser isAdminOrCompanyUser;
    private final IsCompanyExists isCompanyExists;
    private final IsCourseExists isCourseExists;

    public void link(User currentUser, long companyId, long courseId) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminUser.execute(currentUser);
        isCompanyExists.execute(companyId);
        isCourseExists.execute(courseId);

        Optional<LinkCompanyCourse> existentCompanyCourse = companyCourseProvider.linkOf(companyId, courseId);
        if(existentCompanyCourse.isEmpty()) {
            LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(0, companyId, courseId, true, LocalDateTime.now(), null);
            companyCourseProvider.link(linkCompanyCourse);
        } else if(!Objects.isNull(existentCompanyCourse.get().getSuspendedAt())) {
            existentCompanyCourse.get().setActive(true);
            existentCompanyCourse.get().setSuspendedAt(null);

            companyCourseProvider.link(existentCompanyCourse.get());
        }
    }

    public void activeAllByCompanyId(User user, long companyId) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminUser.execute(user);
        isCompanyExists.execute(companyId);

        for(LinkCompanyCourse c : companyCourseProvider.findAllByCompanyId(companyId)) {
            c.setActive(true);
            c.setSuspendedAt(null);
            companyCourseProvider.link(c);
        }
    }

    public Optional<LinkCompanyCourse> find(User user, long companyId, long courseId) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminOrCompanyUser.execute(user, companyId);
        isCompanyExists.execute(companyId);
        isCourseExists.execute(courseId);

        return companyCourseProvider.linkOf(companyId, courseId);
    }

    public Page<LinkCompanyCourse> findAllByCompanyId(User user, int pageNumber, int pageSize, long companyId) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminOrCompanyUser.execute(user, companyId);
        isCompanyExists.execute(companyId);

        return companyCourseProvider.findAllByCompanyIdByPage(pageNumber, pageSize, companyId);
    }

    public void unLink(User user, long companyId, long courseId, boolean delete) throws ForbiddenActionException {
        isAdminOrCompanyAdmin.execute(user, companyId);

        if(delete) {
            Optional<LinkCompanyCourse> companyCourse = companyCourseProvider.linkOf(companyId, courseId);
            companyCourse.ifPresent(companyCourseProvider::unlink);
        } else { // suspend
            Optional<LinkCompanyCourse> companyCourse = companyCourseProvider.linkOf(companyId, courseId, true);
            companyCourse.ifPresent(this::suspendLink);
        }
    }

    public void unLinkAllByCompanyId(User user, long companyId, boolean delete) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminOrCompanyAdmin.execute(user, companyId);
        isCompanyExists.execute(companyId);

        if(delete) {
            companyCourseProvider.unlinkAllByCompanyId(companyId);
        } else { // suspend
            for(LinkCompanyCourse c : companyCourseProvider.findAllByCompanyId(companyId)) {
                suspendLink(c);
            }
        }
    }

    public void unlinkAllByCourseId(User user, long courseId) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminUser.execute(user);
        isCourseExists.execute(courseId);

        companyCourseProvider.unlinkAllByCourseId(courseId);
    }

    void suspendLink(LinkCompanyCourse linkCompanyCourse) {
        if(Objects.isNull(linkCompanyCourse.getSuspendedAt())) {
            linkCompanyCourse.setActive(false);
            linkCompanyCourse.setSuspendedAt(LocalDateTime.now());
            companyCourseProvider.link(linkCompanyCourse);
        }
    }

}
