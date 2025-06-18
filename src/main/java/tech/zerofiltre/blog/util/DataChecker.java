package tech.zerofiltre.blog.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataChecker {

    private final UserProvider userProvider;
    private final CourseProvider courseProvider;
    private final CompanyProvider companyProvider;
    private final CompanyUserProvider companyUserProvider;

    public void checkUserExistence(long userId) throws ResourceNotFoundException {
        userProvider.userOfId(userId).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the user", String.valueOf(userId)));
    }

    public void checkCourseExistence(long courseId) throws ResourceNotFoundException {
        courseProvider.courseOfId(courseId).orElseThrow(() -> new ResourceNotFoundException("We could not find the course", String.valueOf(courseId)));
    }

    public void checkCompanyExistence(long companyId) throws ResourceNotFoundException {
        companyProvider.findById(companyId).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the company", String.valueOf(companyId)));
    }

    public void checkIfAdminUser(User user) throws ForbiddenActionException {
        if(!user.isAdmin())
            throw new ForbiddenActionException("The user must be a Zerofiltre administrator.");
    }

    public void checkIfAdminOrCompanyAdmin(User connectedUser, long companyId) throws ForbiddenActionException {
        if (!isAdminOrCompanyAdmin(connectedUser, companyId)) throw new ForbiddenActionException("You don't have authorization.");
    }

    public void checkIfAdminOrCompanyUser(User connectedUser, long companyId) throws ForbiddenActionException {
        if (!connectedUser.isAdmin()
                && !isCompanyUser(connectedUser.getId(), companyId)) {
            throw new ForbiddenActionException("You don't have authorization.");
        }
    }

    public void checkIfAdminOrCompanyAdminOrEditor(User connectedUser, long companyId) throws ForbiddenActionException {
        if (!connectedUser.isAdmin()
                && !isCompanyAdminOrEditor(connectedUser.getId(), companyId)) {
            throw new ForbiddenActionException("You don't have authorization.");
        }
    }

    public boolean isCompanyAdminOrEditor(long userId, long companyId) {
        Optional<LinkCompanyUser> companyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, userId, true);

        return companyUser.isPresent() && !companyUser.get().getRole().equals(LinkCompanyUser.Role.VIEWER);
    }

    public boolean isCompanyAdmin(long userId, long companyId) {
        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId, true).map(value -> value.getRole().equals(LinkCompanyUser.Role.ADMIN)).orElse(false);
    }

    public boolean isCompanyUser(long userId, long companyId) {
        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId, true).isPresent();
    }

    public boolean isVideoOwner(long courseId, User user) throws ResourceNotFoundException {
        Optional<Course> optionalCourse = courseProvider.courseOfId(courseId);
        if(optionalCourse.isEmpty()){
            throw new ResourceNotFoundException("we could not find the course of id " + courseId);
        }
        if (user.getId() == optionalCourse.get().getAuthor().getId()) {
            return true;
        }
        return false;
    }

    public boolean isAdminOrCompanyAdmin(User connectedUser, long companyId) {
        return connectedUser.isAdmin() || isCompanyAdmin(connectedUser.getId(), companyId);
    }

    Optional<LinkCompanyUser> findCompanyUser(long companyId, long userId) {
        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId);
    }

}
