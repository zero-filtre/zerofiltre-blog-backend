package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NotFoundCourseProviderSpy implements CourseProvider {

    public boolean courseOfIdCalled;
    public boolean registerCourseCalled;

    @Override
    public Optional<Course> courseOfId(long id) {
        courseOfIdCalled = true;
        return Optional.empty();
    }

    @Override
    public Course save(Course course) {
        registerCourseCalled = true;
        return course;
    }

    @Override
    public void delete(Course existingCourse) {

    }

    @Override
    public Page<Course> courseOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag) {
        return null;
    }

    @Override
    public List<Course> courseOf(User foundUser) {
        return Collections.emptyList();
    }

    @Override
    public int getEnrolledCount(long courseId) {
        return 0;
    }

    @Override
    public int getLessonsCount(long courseId) {
        return 0;
    }

    @Override
    public long courseIdOfChapterId(long chapterId) {
        return 0;
    }

    @Override
    public String getTitle(long courseId) { return ""; }

    @Override
    public List<Course> newCoursesFromLastMonth() {
        return List.of();
    }

    @Override
    public Optional<Long> idOfCompanyOwningCourse(long courseId) {
        return Optional.empty();
    }
}
