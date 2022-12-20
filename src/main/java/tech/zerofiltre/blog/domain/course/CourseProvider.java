package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface CourseProvider {
    Optional<Course> courseOfId(long id);
    Course save(Course course);

    void delete(Course existingCourse);

    Page<Course> courseOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag);
}
