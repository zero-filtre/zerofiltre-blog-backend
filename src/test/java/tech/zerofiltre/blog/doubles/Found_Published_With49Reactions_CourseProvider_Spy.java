package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.CLAP;

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
