package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;

import java.util.*;

@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "chapterProvider"})
public class Chapter {
    public static final String AUTHOR_DOES_NOT_EXIST = "The author does not exist";
    public static final String USER_DOES_NOT_EXIST = "The user does not exist";
    public static final String DOES_NOT_EXIST = " does not exist";
    public static final String THE_COURSE_WITH_ID = "The course with id: ";
    public static final String THE_CHAPTER_WITH_ID = "The chapter with id: ";

    private long id;
    private String title;
    private long courseId;
    private int number;
    private List<Lesson> lessons;

    private ChapterProvider chapterProvider;
    private UserProvider userProvider;
    private CourseProvider courseProvider;

    private Chapter(ChapterBuilder chapterBuilder) {
        this.id = chapterBuilder.id;
        this.title = chapterBuilder.title;
        this.courseId = chapterBuilder.courseId;
        this.number = chapterBuilder.number;
        this.lessons = chapterBuilder.lessons;
        this.chapterProvider = chapterBuilder.chapterProvider;
        this.userProvider = chapterBuilder.userProvider;
        this.courseProvider = chapterBuilder.courseProvider;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getCourseId() {
        return courseId;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public int getNumber() {
        return number;
    }

    public ChapterProvider getChapterProvider() {
        return chapterProvider;
    }

    public static ChapterBuilder builder() {
        return new ChapterBuilder();
    }

    public Chapter init(String title, long courseId, long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException("You are not allowed to create a chapter for this course", Domains.COURSE.name());
        }

        this.title = title;
        this.courseId = courseId;

        return setProviders(chapterProvider.save(this));
    }


    private boolean isNotAdmin(User existingUser) {
        return !existingUser.getRoles().contains("ROLE_ADMIN");
    }

    private Chapter setProviders(Chapter chapter) {
        chapter.chapterProvider = chapterProvider;
        chapter.userProvider = userProvider;
        chapter.courseProvider = courseProvider;
        return chapter;
    }

    public Chapter save(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException("You are not allowed to edit a chapter for this course", Domains.COURSE.name());
        }

        Chapter existingChapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        String titleToSave = title;

        setAttributes(existingChapter);
        this.title = titleToSave;
        return setProviders(chapterProvider.save(this));
    }

    private void setAttributes(Chapter existingChapter) {
        this.id = existingChapter.getId();
        this.title = existingChapter.getTitle();
        this.lessons = existingChapter.getLessons();
        this.courseId = existingChapter.getCourseId();
        this.number = existingChapter.getNumber();
    }

    public void delete(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter existingChapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));
        if (!existingChapter.getLessons().isEmpty())
            throw new ForbiddenActionException("You are not allowed to delete a chapter with lessons", Domains.COURSE.name());

        Course existingCourse = courseProvider.courseOfId(existingChapter.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException("You are not allowed to delete a chapter for this course", Domains.COURSE.name());
        }

        chapterProvider.delete(existingChapter);
    }

    public Chapter get() throws ResourceNotFoundException {
        return setProviders(chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name())));
    }

    public List<Chapter> getByCourseId(User user) throws ResourceNotFoundException, ForbiddenActionException {
        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), Domains.COURSE.name()));

        if (
                (user == null && Status.PUBLISHED != existingCourse.getStatus())
                        || (user != null && isNotAdmin(user) && existingCourse.getAuthor().getId() != user.getId() && Status.PUBLISHED != existingCourse.getStatus())
        )
            throw new ForbiddenActionException("You are not allowed to get chapters for this course", Domains.COURSE.name());
        return chapterProvider.ofCourseId(courseId);
    }


    public static class ChapterBuilder {
        private int number;
        private UserProvider userProvider;
        private CourseProvider courseProvider;
        private long id;
        private String title;
        private long courseId;
        private List<Lesson> lessons = new ArrayList<>();
        private ChapterProvider chapterProvider;

        public ChapterBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ChapterBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ChapterBuilder courseId(long courseId) {
            this.courseId = courseId;
            return this;
        }

        public ChapterBuilder lessons(List<Lesson> lessons) {
            this.lessons = lessons;
            return this;
        }

        public ChapterBuilder chapterProvider(ChapterProvider chapterProvider) {
            this.chapterProvider = chapterProvider;
            return this;
        }

        public ChapterBuilder userProvider(UserProvider userProvider) {
            this.userProvider = userProvider;
            return this;
        }

        public ChapterBuilder courseProvider(CourseProvider courseProvider) {
            this.courseProvider = courseProvider;
            return this;
        }

        public ChapterBuilder number(int number) {
            this.number = number;
            return this;
        }

        public Chapter build() {
            return new Chapter(this);
        }
    }

}
