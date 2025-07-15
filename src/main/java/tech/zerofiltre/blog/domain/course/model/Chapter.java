package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static tech.zerofiltre.blog.domain.course.model.Lesson.THE_LESSON_OF_ID;


@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "chapterProvider", "checker"})
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
    private DataChecker checker;

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
        this.checker = chapterBuilder.checker;
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
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        checkUserAuthorized(existingUser, existingCourse, "You are not allowed to create a chapter for this course");

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
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        checkUserAuthorized(existingUser, existingCourse, YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE);

        Chapter existingChapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        String titleToSave = title;

        setAttributes(existingChapter);
        this.title = titleToSave;
        return setProviders(chapterProvider.save(this));
    }

    public void delete(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter existingChapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));
        if (!existingChapter.getLessons().isEmpty())
            throw new ForbiddenActionException("You are not allowed to delete a chapter with lessons");

        Course existingCourse = courseProvider.courseOfId(existingChapter.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        if (Status.PUBLISHED == existingCourse.getStatus()) {
            Optional<Long> companyId = courseProvider.idOfCompanyOwningCourse(existingCourse.getId());

            if (!existingUser.isAdmin()
                    && (companyId.isEmpty()
                    || !checker.isCompanyAdmin(existingUser.getId(), companyId.get())
            )
            )
                throw new ForbiddenActionException("You are not allowed to delete a chapter for a published course, please get in touch with an admin");
        }

        if (Status.PUBLISHED != existingCourse.getStatus()) {
            checkUserAuthorized(existingUser, existingCourse, "You are not allowed to delete a chapter for this course");
        }

        chapterProvider.delete(existingChapter);
    }

    public Chapter get() throws ResourceNotFoundException {
        return setProviders(chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id))));
    }

    public List<Chapter> getByCourseId(User user) throws ResourceNotFoundException, ForbiddenActionException {
        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        if (Status.PUBLISHED != existingCourse.getStatus() && user == null)
            throw new ForbiddenActionException("You are not allowed to get chapters for this course");

        if (Status.PUBLISHED != existingCourse.getStatus() && user != null) {
            checkUserAuthorized(user, existingCourse, "You are not allowed to get chapters for this course");
            return chapterProvider.ofCourseId(courseId);
        }

        if (user != null && user.isAdmin()) return chapterProvider.ofCourseId(courseId);

        if (user != null && !user.isAdmin()) {
            Optional<Long> companyId = courseProvider.idOfCompanyOwningCourse(existingCourse.getId());
            if (companyId.isPresent()) checker.checkIfAdminOrCompanyUser(user, companyId.get());
            return chapterProvider.ofCourseId(courseId);
        }

        if (courseProvider.idOfCompanyOwningCourse(existingCourse.getId()).isPresent()) {
            throw new ForbiddenActionException("You are not allowed to get chapters for this course");
        }

        return chapterProvider.ofCourseId(courseId);
    }

    public Chapter moveLesson(long currentUserId, long lessonId, int toNumber) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(AUTHOR_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter chapter = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        long theCourseId = chapter.getCourseId();
        Course existingCourse = courseProvider.courseOfId(theCourseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + theCourseId + DOES_NOT_EXIST, String.valueOf(theCourseId)));

        checkUserAuthorized(existingUser, existingCourse, YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE);

        List<Lesson> lessonList = chapter.getLessons();
        Lesson lessonToMove = null;
        for (Lesson lesson : lessonList) {
            if (lesson.getId() == lessonId)
                lessonToMove = lesson;
        }
        if (lessonToMove == null)
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + lessonId + DOES_NOT_EXIST, String.valueOf(lessonId));

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


    public Chapter move(long currentUserId, int toNumber) throws ZerofiltreException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        Chapter chapterToMove = chapterProvider.chapterOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_CHAPTER_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        long theCourseId = chapterToMove.getCourseId();
        Course existingCourse = courseProvider.courseOfId(theCourseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + theCourseId + DOES_NOT_EXIST, String.valueOf(theCourseId)));

        checkUserAuthorized(existingUser, existingCourse, YOU_ARE_NOT_ALLOWED_TO_EDIT_A_CHAPTER_FOR_THIS_COURSE);

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
        Chapter result = chapterList.stream().filter(chapter -> chapter.getId() == chapterToMove.getId()).findFirst().orElseThrow(() -> new ZerofiltreException("An error occurred when moving the chapter, try again"));
        return setProviders(result);
    }

    private void checkUserAuthorized(User existingUser, Course existingCourse, String message) throws ForbiddenActionException {
        Optional<Long> companyId = courseProvider.idOfCompanyOwningCourse(existingCourse.getId());

        if (companyId.isPresent()) {
            checker.checkIfAdminOrCompanyAdminOrEditor(existingUser, companyId.get());
            return;
        }

        if (!existingUser.isAdmin()
                && existingCourse.getAuthor().getId() != existingUser.getId())
            throw new ForbiddenActionException(message);
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
        chapter.checker = checker;
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
        private LessonProvider lessonProvider;
        private DataChecker checker;


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

        public ChapterBuilder checker(DataChecker checker) {
            this.checker = checker;
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
