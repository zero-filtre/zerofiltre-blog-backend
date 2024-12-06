package tech.zerofiltre.blog.domain.course.features.course;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;

import java.util.Optional;

@RequiredArgsConstructor
public class IsPublishedCourse {

    private final CourseProvider courseProvider;

    public boolean execute(long courseId) throws ForbiddenActionException {
        Optional<Course> course = courseProvider.courseOfId(courseId);

        if(course.isPresent() && !course.get().getStatus().equals(Status.PUBLISHED)) {
            throw new ForbiddenActionException("This course is not published.");
        }
        return true;
    }

}
