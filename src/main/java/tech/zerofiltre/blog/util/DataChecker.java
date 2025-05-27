package tech.zerofiltre.blog.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
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
    private final CompanyCourseProvider companyCourseProvider;

    public boolean userExists(long userId) throws ResourceNotFoundException {
        userProvider.userOfId(userId).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the user", String.valueOf(userId)));

        return true;
    }

    public boolean isAdminUser(User user) throws ForbiddenActionException {
        if(!user.isAdmin())
            throw new ForbiddenActionException("The user must be a Zerofiltre administrator.");

        return true;
    }

    public boolean courseExists(long courseId) throws ResourceNotFoundException {
        Optional<Course> course = courseProvider.courseOfId(courseId);

        if(course.isEmpty()) {
            throw new ResourceNotFoundException("We could not find the course", String.valueOf(courseId));
        }
        return true;
    }

    public boolean isPublishedCourse(long courseId) throws ForbiddenActionException {
        Optional<Course> course = courseProvider.courseOfId(courseId);

        if(course.isPresent() && !course.get().getStatus().equals(Status.PUBLISHED)) {
            throw new ForbiddenActionException("This course is not published.");
        }
        return true;
    }

    public boolean companyExists(long companyId) throws ResourceNotFoundException {
        companyProvider.findById(companyId).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the company", String.valueOf(companyId)));

        return true;
    }

    public boolean companyUserExists(long companyId, long userId) throws ResourceNotFoundException {
        if (findCompanyUser(companyId, userId).isEmpty()) {
            throw new ResourceNotFoundException("We could not find the company user", String.valueOf(userId));
        }
        return true;
    }

    public boolean companyCourseExists(long companyId, long courseId) throws ResourceNotFoundException {
        Optional<LinkCompanyCourse> companyCourse = companyCourseProvider.findByCompanyIdAndCourseId(companyId, courseId);

        if(companyCourse.isEmpty()) {
            throw new ResourceNotFoundException("We could not find the company course", String.valueOf(courseId));
        }
        if(!companyCourse.get().isActive()) {
            throw new ResourceNotFoundException("This course is not active", String.valueOf(courseId));
        }
        return true;
    }

    public boolean isAdminOrCompanyAdmin(User connectedUser, long companyId) throws ForbiddenActionException {
        if (!connectedUser.isAdmin()
                && !isCompanyAdmin(connectedUser.getId(), companyId)) {
                throw new ForbiddenActionException("You don't have authorization.");
        }
        return true;
    }

    public boolean isAdminOrCompanyUser(User connectedUser, long companyId) throws ForbiddenActionException {
        if (!connectedUser.isAdmin()
            && !isCompanyUser(connectedUser.getId(), companyId)) {
                throw new ForbiddenActionException("You don't have authorization.");
        }
        return true;
    }

    public boolean isCompanyAdminOrEditor(long userId, long companyId) {
        Optional<LinkCompanyUser> companyUser = findCompanyUser(companyId, userId);

        return companyUser.isPresent() && !companyUser.get().getRole().equals(LinkCompanyUser.Role.VIEWER);
    }

    public boolean isAdminOrCompanyAdminOrEditor(User connectedUser, long companyId) throws ForbiddenActionException {
        if (!connectedUser.isAdmin()
            && !isCompanyAdminOrEditor(connectedUser.getId(), companyId)) {
                throw new ForbiddenActionException("You don't have authorization.");
        }
        return true;
    }

    public boolean isCompanyAdmin(long userId, long companyId) {
        return findCompanyUser(companyId, userId).map(value -> value.getRole().equals(LinkCompanyUser.Role.ADMIN)).orElse(false);
    }

    public boolean isCompanyUser(long userId, long companyId) {
        return findCompanyUser(companyId, userId).isPresent();
    }

    Optional<LinkCompanyUser> findCompanyUser(long companyId, long userId) {
        return companyUserProvider.findByCompanyIdAndUserId(companyId, userId, true);
    }

}
