package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;

import java.util.*;

import static tech.zerofiltre.blog.domain.Domains.*;
import static tech.zerofiltre.blog.domain.course.model.Lesson.*;

@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "chapterProvider"})
public class Chapter {
    public static final String AUTHOR_DOES_NOT_EXIST = "The author does not exist";
    public static final String USER_DOES_NOT_EXIST = "The user does not exist";
    public static final String DOES_NOT_EXIST = " does not exist";
    public static final String THE_COURSE_WITH_ID = "The course with id: ";
    public static final String THE_CHAPTER_WITH_ID = "The chapter with id: ";
    public static final String YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE = "You are not allowed to edit a chapter for this course";

    private long id;
    private String title;
    private long courseId;
    private int number;
    private List<Lesson> lessons;

    private ChapterProvider chapterProvider;
    private UserProvider userProvider;
    private CourseProvider courseProvider;
    private LessonProvider lessonProvider;

    private Chapter(ChapterBuilder chapterBuilder) {
        this.id = chapterBuilder.id;
        this.title = chapterBuilder.title;
        this.courseId = chapterBuilder.courseId;
        this.number = chapterBuilder.number;
        this.lessons = chapterBuilder.lessons;
        this.chapterProvider = chapterBuilder.chapterProvider;
        this.userProvider = chapterBuilder.userProvider;
        this.courseProvider = chapterBuilder.courseProvider;
        this.lessonProvider = chapterBuilder.lessonProvider;
    }

    //DATA

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

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public void setNumber(int number) {
        this.number = number;
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

    //LOGIC

    public Chapter init(String title, long courseId, long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException("You are not allowed to create a chapter for this course", COURSE.name());
        }

        this.title = title;
        this.courseId = courseId;

        Chapter lastChapter = chapterProvider.ofCourseId(courseId).stream().reduce((first, second) -> second).orElse(null);
        this.number = (lastChapter != null) ? lastChapter.getNumber() + 1 : 1;

        return setProviders(chapterProvider.save(this));
    }

    public Chapter save(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException(YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE, COURSE.name());
        }

        Chapter existingChapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));

        String titleToSave = title;

        setAttributes(existingChapter);
        this.title = titleToSave;
        return setProviders(chapterProvider.save(this));
    }

    public void delete(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter existingChapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));
        if (!existingChapter.getLessons().isEmpty())
            throw new ForbiddenActionException("You are not allowed to delete a chapter with lessons", COURSE.name());

        Course existingCourse = courseProvider.courseOfId(existingChapter.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));

        if (existingCourse.getStatus() == Status.PUBLISHED && !existingUser.isAdmin())
            throw new ForbiddenActionException("You are not allowed to delete a chapter for a published course, please get in touch with an admin", COURSE.name());

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException("You are not allowed to delete a chapter for this course", COURSE.name());
        }

        chapterProvider.delete(existingChapter);
    }

    public Chapter get() throws ResourceNotFoundException {
        return setProviders(chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name())));
    }

    public List<Chapter> getByCourseId(User user) throws ResourceNotFoundException, ForbiddenActionException {
        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));

        if (
                (user == null && Status.PUBLISHED != existingCourse.getStatus())
                        || (user != null && isNotAdmin(user) && existingCourse.getAuthor().getId() != user.getId() && Status.PUBLISHED != existingCourse.getStatus())
        )
            throw new ForbiddenActionException("You are not allowed to get chapters for this course", COURSE.name());
        return chapterProvider.ofCourseId(courseId);
    }

    public Chapter moveLesson(long currentUserId, long lessonId, int toNumber) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter chapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));

        long theCourseId = chapter.getCourseId();
        Course existingCourse = courseProvider.courseOfId(theCourseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + theCourseId + DOES_NOT_EXIST, String.valueOf(theCourseId), ""));

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException(YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE, COURSE.name());
        }
        List<Lesson> lessonList = chapter.getLessons();
        Lesson lessonToMove = null;
        for (Lesson lesson : lessonList) {
            if (lesson.getId() == lessonId)
                lessonToMove = lesson;
        }
        if (lessonToMove == null)
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + lessonId + DOES_NOT_EXIST, String.valueOf(lessonId), COURSE.name());

        int currentPosition = lessonToMove.getNumber();

        // Move the lesson to the new position
        lessonToMove.setNumber(toNumber);

        // Update the positions of the other lessons
        for (Lesson lesson : lessonList) {
            if (lesson.getId() != lessonToMove.getId()) {
                int lPosition = lesson.getNumber();
                if (currentPosition < toNumber) {
                    // Shift lessons down
                    if (lPosition > currentPosition && lPosition <= toNumber) {
                        lesson.setNumber(lPosition - 1);
                    }
                } else {
                    // Shift lessons up
                    if (lPosition < currentPosition && lPosition >= toNumber) {
                        lesson.setNumber(lPosition + 1);
                    }
                }
            }
        }
        chapter.setLessons(lessonProvider.saveAll(lessonList));
        return setProviders(chapter);
    }


    public Chapter move(long currentUserId, int toNumber) throws BlogException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter chapterToMove = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));

        long theCourseId = chapterToMove.getCourseId();
        Course existingCourse = courseProvider.courseOfId(theCourseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + theCourseId + DOES_NOT_EXIST, String.valueOf(theCourseId), ""));

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId()) {
            throw new ForbiddenActionException(YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE, COURSE.name());
        }

        List<Chapter> chapterList = chapterProvider.ofCourseId(theCourseId);
        int currentNumber = chapterToMove.getNumber();

        for (Chapter aChapter : chapterList) {
            if (aChapter.getId() == chapterToMove.getId()) {
                // Move the chapter to the new position
                aChapter.setNumber(toNumber);
            } else {
                int aChapterNumber = aChapter.getNumber();
                if (currentNumber < toNumber) {
                    // Shift chapters down
                    if (aChapterNumber > currentNumber && aChapterNumber <= toNumber) {
                        aChapter.setNumber(aChapterNumber - 1);
                    }
                } else {
                    // Shift chapters up
                    if (aChapterNumber < currentNumber && aChapterNumber >= toNumber) {
                        aChapter.setNumber(aChapterNumber + 1);
                    }
                }
            }
        }
        chapterList = chapterProvider.saveAll(chapterList);
        Chapter result = chapterList.stream().filter(chapter -> chapter.getId() == chapterToMove.getId()).findFirst().orElseThrow(() -> new BlogException("An error occurred when moving the chapter, try again", ""));
        return setProviders(result);
    }

    private boolean isNotAdmin(User existingUser) {
        return !existingUser.getRoles().contains("ROLE_ADMIN");
    }

    private void setAttributes(Chapter existingChapter) {
        this.id = existingChapter.getId();
        this.title = existingChapter.getTitle();
        this.lessons = existingChapter.getLessons();
        this.courseId = existingChapter.getCourseId();
        this.number = existingChapter.getNumber();
    }

    private Chapter setProviders(Chapter chapter) {
        chapter.chapterProvider = chapterProvider;
        chapter.userProvider = userProvider;
        chapter.courseProvider = courseProvider;
        chapter.lessonProvider = lessonProvider;
        return chapter;
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
        public LessonProvider lessonProvider;


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

        public ChapterBuilder lessonProvider(LessonProvider lessonProvider) {
            this.lessonProvider = lessonProvider;
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
