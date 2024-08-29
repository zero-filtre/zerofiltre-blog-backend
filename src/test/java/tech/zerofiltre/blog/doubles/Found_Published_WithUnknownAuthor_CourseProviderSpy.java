package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Found_Published_WithUnknownAuthor_CourseProviderSpy implements CourseProvider {

    public boolean courseOfIdCalled;
    public boolean registerCourseCalled;

    @Override
    public Optional<Course> courseOfId(long id) {
        courseOfIdCalled = true;
        User author = new User();
        author.setId(21L);
        author.setEmail("just@weirdemail.com");
        return Optional.of(ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList()));
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
}
