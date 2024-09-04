package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static tech.zerofiltre.blog.domain.course.model.Chapter.DOES_NOT_EXIST;
import static tech.zerofiltre.blog.domain.course.model.Chapter.USER_DOES_NOT_EXIST;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.VIDEO_NOT_AVAILABLE_FOR_FREE;
import static tech.zerofiltre.blog.domain.error.ErrorMessages.YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED;

@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "chapterProvider", "lessonProvider", "enrollmentProvider"})
public class Lesson {

    public static final String THE_LESSON_OF_ID = "The lesson of id ";

    private long id;
    private String title;
    private String content;
    private String summary;
    private String thumbnail;
    private String video;
    private boolean free;
    private long chapterId;
    private int number;
    private List<Resource> resources;

    private LessonProvider lessonProvider;
    private ChapterProvider chapterProvider;
    private UserProvider userProvider;
    private CourseProvider courseProvider;
    private EnrollmentProvider enrollmentProvider;
    private boolean notEnrolledAccess;


    private User currentUser;
    private Chapter chapter;
    private Course course;


    private Lesson(LessonBuilder lessonBuilder) {
        this.id = lessonBuilder.id;
        this.title = lessonBuilder.title;
        this.content = lessonBuilder.content;
        this.summary = lessonBuilder.summary;
        this.thumbnail = lessonBuilder.thumbnail;
        this.video = lessonBuilder.video;
        this.free = lessonBuilder.free;
        this.chapterId = lessonBuilder.chapterId;
        this.resources = lessonBuilder.resources;
        this.lessonProvider = lessonBuilder.lessonProvider;
        this.chapterProvider = lessonBuilder.chapterProvider;
        this.userProvider = lessonBuilder.userProvider;
        this.courseProvider = lessonBuilder.courseProvider;
        this.enrollmentProvider = lessonBuilder.enrollmentProvider;
        this.number = lessonBuilder.number;
    }

    public long getId() {
        return id;
    }

    public void copyData(Lesson lesson) {
        this.id = lesson.id;
        this.title = lesson.title;
        this.content = lesson.content;
        this.summary = lesson.summary;
        this.thumbnail = lesson.thumbnail;
        this.video = lesson.video;
        this.free = lesson.free;
        this.chapterId = lesson.chapterId;
        this.resources = lesson.resources;
        this.number = lesson.number;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getVideo() {
        return notEnrolledAccess ? VIDEO_NOT_AVAILABLE_FOR_FREE : video;
    }

    public boolean isFree() {
        return free;
    }

    public String getType() {
        if (video != null && !video.isEmpty())
            return "video";
        return "text";
    }

    public long getChapterId() {
        return chapterId;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public static LessonBuilder builder() {
        return new LessonBuilder();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Lesson init(String title, long chapterId, long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        checkLessonAccessConditions(currentUserId, chapterId, false, false);
        this.title = title;
        this.chapterId = chapterId;

        Lesson lastLesson = chapter.getLessons().stream().reduce((first, second) -> second).orElse(null);

        int newPosition = (lastLesson != null) ? lastLesson.getNumber() + 1 : 1;
        this.setNumber(newPosition);

        return setProviders(lessonProvider.save(this));
    }

    public Lesson save(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {

        Lesson lesson = lessonProvider.lessonOfId(this.id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_LESSON_OF_ID + id + DOES_NOT_EXIST, String.valueOf(id)));
        checkLessonAccessConditions(currentUserId, chapterId, false, false);
        String titleToSave = this.title;
        String contentToSave = this.content;
        String summaryToSave = this.summary;
        String thumbnailToSave = this.thumbnail;
        String videoToSave = this.video;
        boolean freeToSave = this.free;

        copyData(lesson);
        setAttributes(titleToSave, contentToSave, summaryToSave, thumbnailToSave, videoToSave, freeToSave);
        return setProviders(lessonProvider.save(this));

    }

    private void setAttributes(String titleToSave, String contentToSave, String summaryToSave, String thumbnailToSave, String videoToSave, boolean freeToSave) {
        this.title = titleToSave;
        this.content = contentToSave;
        this.summary = summaryToSave;
        this.thumbnail = thumbnailToSave;
        this.video = videoToSave;
        this.free = freeToSave;
    }

    public void delete(long currentUserId) throws ForbiddenActionException, ResourceNotFoundException {
        Optional<Lesson> lesson = lessonProvider.lessonOfId(this.id);
        if (lesson.isEmpty()) {
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + id + DOES_NOT_EXIST, String.valueOf(id));
        }
        checkLessonAccessConditions(currentUserId, lesson.get().getChapterId(), true, false);
        lessonProvider.delete(lesson.get());
    }

    public Lesson get(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        Optional<Lesson> lesson = lessonProvider.lessonOfId(this.id);

        if (lesson.isEmpty())
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + id + DOES_NOT_EXIST, String.valueOf(id));

        // THE USER IS CONNECTED

        if (currentUserId != 0) {
            this.free = lesson.get().isFree();
            checkLessonAccessConditions(currentUserId, lesson.get().getChapterId(), false, true);
            return setProviders(lesson.get());
        }

        // THE USER IS NOT CONNECTED

        chapter = chapterProvider.chapterOfId(lesson.get().getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("The chapter with id: " + lesson.get().getChapterId() + DOES_NOT_EXIST, String.valueOf(lesson.get().getChapterId())));

        long courseId = chapter.getCourseId();
        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + courseId + DOES_NOT_EXIST, String.valueOf(courseId)));

        if (!existingCourse.getStatus().equals(Status.PUBLISHED)) {
            throw new ForbiddenActionException(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
        }

        if (!lesson.get().isFree()) notEnrolledAccess = true;

        return setProviders(lesson.get());
    }

    public ChapterProvider getChapterProvider() {
        return chapterProvider;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    public CourseProvider getCourseProvider() {
        return courseProvider;
    }

    public EnrollmentProvider getEnrollmentProvider() {
        return enrollmentProvider;
    }


    private void checkLessonAccessConditions(long currentUserId, long chapterId, boolean isDeletion, boolean checkEnrollments) throws ResourceNotFoundException, ForbiddenActionException {
        currentUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(currentUserId)));

        chapter = chapterProvider.chapterOfId(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("The chapter with id: " + chapterId + DOES_NOT_EXIST, String.valueOf(chapterId)));

        long courseId = chapter.getCourseId();
        course = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + courseId + DOES_NOT_EXIST, String.valueOf(courseId)));

        if (!currentUser.isAdmin() && course.getAuthor().getId() != currentUser.getId() && !course.getStatus().equals(Status.PUBLISHED)) {
            throw new ForbiddenActionException(YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED);
        }

        if (isDeletion && course.getStatus().equals(Status.PUBLISHED) && !currentUser.isAdmin())
            throw new ForbiddenActionException("You can not delete a lesson that is already published");

        if (!currentUser.isAdmin() && course.getAuthor().getId() != currentUser.getId() && !checkEnrollments) {
            throw new ForbiddenActionException("You are not allowed to do this action on this course");
        }

        if (checkEnrollments) {
            Optional<Enrollment> enrollment = enrollmentProvider.enrollmentOf(currentUserId, courseId, true);
            if (!currentUser.isAdmin() && course.getAuthor().getId() != currentUser.getId() && enrollment.isEmpty() && !free)
                notEnrolledAccess = true;
        }
    }


    private Lesson setProviders(Lesson lesson) {
        lesson.lessonProvider = this.lessonProvider;
        lesson.chapterProvider = this.chapterProvider;
        lesson.userProvider = this.userProvider;
        lesson.courseProvider = this.courseProvider;
        lesson.enrollmentProvider = this.enrollmentProvider;
        lesson.notEnrolledAccess = this.notEnrolledAccess;
        return lesson;
    }


    public static class LessonBuilder {
        private int number;
        private long id;
        private String title;
        private String content;
        private String summary;
        private String thumbnail;
        private String video;
        private boolean free;
        private long chapterId;
        private List<Resource> resources = new ArrayList<>();
        private LessonProvider lessonProvider;
        private ChapterProvider chapterProvider;
        private UserProvider userProvider;
        private CourseProvider courseProvider;
        private EnrollmentProvider enrollmentProvider;


        public LessonBuilder id(long id) {
            this.id = id;
            return this;
        }

        public LessonBuilder title(String title) {
            this.title = title;
            return this;
        }

        public LessonBuilder content(String content) {
            this.content = content;
            return this;
        }

        public LessonBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public LessonBuilder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public LessonBuilder video(String video) {
            this.video = video;
            return this;
        }

        public LessonBuilder free(boolean free) {
            this.free = free;
            return this;
        }

        public LessonBuilder chapterId(long chapterId) {
            this.chapterId = chapterId;
            return this;
        }

        public LessonBuilder number(int number) {
            this.number = number;
            return this;
        }

        public LessonBuilder resources(List<Resource> resources) {
            this.resources = resources;
            return this;
        }

        public LessonBuilder lessonProvider(LessonProvider lessonProvider) {
            this.lessonProvider = lessonProvider;
            return this;
        }

        public LessonBuilder chapterProvider(ChapterProvider chapterProvider) {
            this.chapterProvider = chapterProvider;
            return this;
        }

        public LessonBuilder userProvider(UserProvider userProvider) {
            this.userProvider = userProvider;
            return this;
        }

        public LessonBuilder courseProvider(CourseProvider courseProvider) {
            this.courseProvider = courseProvider;
            return this;
        }

        public LessonBuilder enrollmentProvider(EnrollmentProvider enrollmentProvider) {
            this.enrollmentProvider = enrollmentProvider;
            return this;
        }

        public Lesson build() {
            return new Lesson(this);
        }
    }

}
