package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;

@RequiredArgsConstructor
public class CompanyService {

    private final CompanyProvider companyProvider;
    private final CourseProvider courseProvider;

    public Course addCourse(long companyId, User user, Course course) throws ForbiddenActionException, UserNotFoundException {
        if(!user.isAdmin() && !user.isCompanyAdmin() && !user.isCompanyEditor())
            throw new ForbiddenActionException("You are not authorized to add a course.");

        if(!companyProvider.isUserPartOfCompany(companyId, user.getId()))
            throw new UserNotFoundException("The user is not part of the company.", "");

        return null;
    }
}
