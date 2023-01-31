package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

public class Found_Draft_WithKnownAuthor_CourseProvider_Spy implements CourseProvider {

    public boolean courseOfIdCalled;
    public boolean saveCalled;
    public boolean deleteCalled;

    @Override
    public Optional<Course> courseOfId(long id) {
        courseOfIdCalled = true;
        User author = ZerofiltreUtils.createMockUser(false);
        return Optional.of(ZerofiltreUtils.createMockCourse(true,Status.DRAFT, author, Collections.emptyList(),Collections.emptyList()));
    }

    @Override
    public Course save(Course course) {
        saveCalled = true;
        return course;
    }

    @Override
    public void delete(Course existingCourse) {
        deleteCalled = true;
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
}
