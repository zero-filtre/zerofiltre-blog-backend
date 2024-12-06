package tech.zerofiltre.blog.domain.course.features.course;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IsCourseExists {

    private final CourseProvider courseProvider;

    public boolean execute(long courseId) throws ResourceNotFoundException {
        Optional<Course> course = courseProvider.courseOfId(courseId);

        if(course.isEmpty()) {
            throw new ResourceNotFoundException("We could not find the course", String.valueOf(courseId));
        }
        return true;
    }

}
