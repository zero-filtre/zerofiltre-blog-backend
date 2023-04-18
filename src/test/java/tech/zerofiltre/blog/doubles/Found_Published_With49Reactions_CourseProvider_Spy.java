package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.*;

public class Found_Published_With49Reactions_CourseProvider_Spy implements CourseProvider {

    public boolean courseOfIdCalled;
    public boolean registerCourseCalled;

    @Override
    public Optional<Course> courseOfId(long id) {
        courseOfIdCalled = true;
        User author = ZerofiltreUtils.createMockUser(false);
        List<Reaction> currentUserReactions = new ArrayList<>();
        for (int i = 0; i < 49; i++) {
            Reaction reaction = new Reaction();
            reaction.setAuthorId(author.getId());
            reaction.setAction(CLAP);
            currentUserReactions.add(reaction);
        }
        return Optional.of(ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), currentUserReactions));
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
        return new ArrayList<>();
    }

    @Override
    public int getEnrolledCount(long courseId) {
        return 0;
    }
}
