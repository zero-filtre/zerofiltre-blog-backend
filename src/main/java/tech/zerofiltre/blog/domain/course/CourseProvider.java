package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.List;
import java.util.Optional;

public interface CourseProvider {
    Optional<Course> courseOfId(long id);

    Course save(Course course);

    void delete(Course existingCourse);

    Page<Course> courseOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag);

    List<Course> courseOf(User foundUser);

    int getEnrolledCount(long courseId);

    int getLessonsCount(long courseId);

    long courseIdOfChapterId(long chapterId);

    String getTitle(long courseId);

    List<Course> newCoursesFromLastMonth();
}
