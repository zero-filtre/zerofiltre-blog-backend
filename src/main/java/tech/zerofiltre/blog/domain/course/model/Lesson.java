package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

import static tech.zerofiltre.blog.domain.Domains.*;
import static tech.zerofiltre.blog.domain.course.model.Chapter.*;

@JsonIgnoreProperties(value = {"courseProvider", "userProvider", "chapterProvider", "lessonProvider", "enrollmentProvider"})
public class Lesson {

    public static final String THE_LESSON_OF_ID = "The lesson of id ";
    public static final String VIDEO_NOT_AVAILABLE_FOR_FREE = "VIDEO NOT AVAILABLE FOR FREE";
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

    public static LessonBuilder builder() {
        return new LessonBuilder();
    }

    public int getNumber() {
        return number;
    }

    public Lesson init(String title, long chapterId, long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        checkConditions(currentUserId, chapterId, false, false);
        this.title = title;
        this.chapterId = chapterId;


        return setProviders(lessonProvider.save(this));
    }

    public Lesson save(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {

        Lesson lesson = lessonProvider.lessonOfId(this.id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_LESSON_OF_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name()));
        checkConditions(currentUserId, chapterId, false, false);
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
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name());
        }
        checkConditions(currentUserId, lesson.get().getChapterId(), true, false);
        lessonProvider.delete(lesson.get());
    }

    public Lesson get(long currentUserId) throws ResourceNotFoundException, ForbiddenActionException {
        Optional<Lesson> lesson = lessonProvider.lessonOfId(this.id);
        if (lesson.isEmpty())
            throw new ResourceNotFoundException(THE_LESSON_OF_ID + id + DOES_NOT_EXIST, String.valueOf(id), COURSE.name());

        if (currentUserId == 0 && lesson.get().isFree()) {
            return setProviders(lesson.get());
        } else if (currentUserId == 0 && !lesson.get().isFree()) {
            notEnrolledAccess = true;
            return setProviders(lesson.get());
        }

        checkConditions(currentUserId, lesson.get().getChapterId(), false, true);
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


    private void checkConditions(long currentUserId, long chapterId, boolean isDeletion, boolean checkEnrollments) throws ResourceNotFoundException, ForbiddenActionException {
        User existingUser = userProvider.userOfId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(currentUserId), COURSE.name()));

        Chapter existingChapter = chapterProvider.chapterOfId(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("The chapter with id: " + chapterId + DOES_NOT_EXIST, String.valueOf(chapterId), COURSE.name()));

        long courseId = existingChapter.getCourseId();
        Course existingCourse = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("The course with id: " + courseId + DOES_NOT_EXIST, String.valueOf(courseId), COURSE.name()));

        if (isDeletion && existingCourse.getStatus().equals(Status.PUBLISHED) && !existingUser.isAdmin())
            throw new ForbiddenActionException("You can not delete a lesson that is already published", Domains.COURSE.name());

        if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId() && !checkEnrollments) {
            throw new ForbiddenActionException("You are not allowed to do this action on this course", Domains.COURSE.name());
        }

        if (checkEnrollments) {
            Optional<Enrollment> enrollment = enrollmentProvider.enrollmentOf(currentUserId, courseId, true);
            if (!existingUser.isAdmin() && existingCourse.getAuthor().getId() != existingUser.getId() && enrollment.isEmpty())
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
