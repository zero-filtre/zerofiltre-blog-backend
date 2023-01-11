package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.user.*;

import java.util.*;

public class Lesson {

    private long id;
    private String title;
    private String content;
    private String summary;
    private String thumbnail;
    private String  video;
    private String  free;
    private String  type;
    private long chapterId;
    private List<Resource> resources = new ArrayList<>();
    private LessonProvider lessonProvider;
    private ChapterProvider chapterProvider;
    private UserProvider userProvider;
    private CourseProvider courseProvider;


    private Lesson(LessonBuilder lessonBuilder) {
        this.id = lessonBuilder.id;
        this.title = lessonBuilder.title;
        this.content = lessonBuilder.content;
        this.summary = lessonBuilder.summary;
        this.thumbnail = lessonBuilder.thumbnail;
        this.video = lessonBuilder.video;
        this.free = lessonBuilder.free;
        this.type = lessonBuilder.type;
        this.chapterId = lessonBuilder.chapterId;
        this.resources = lessonBuilder.resources;
        this.lessonProvider = lessonBuilder.lessonProvider;
        this.chapterProvider = lessonBuilder.chapterProvider;
        this.userProvider = lessonBuilder.userProvider;
        this.courseProvider = lessonBuilder.courseProvider;
    }

    public long getId() {
        return id;
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
        return video;
    }

    public String getFree() {
        return free;
    }

    public String getType() {
        return type;
    }

    public long getChapterId() {
        return chapterId;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public LessonProvider getLessonProvider() {
        return lessonProvider;
    }

    public static LessonBuilder builder() {
        return new LessonBuilder();
    }

    public Lesson init(String title, long chapterId, long currentUserId) {
        this.title = title;
        this.chapterId = chapterId;


        return setProviders(lessonProvider.save(this));
    }

    private Lesson setProviders(Lesson lesson) {
        lesson.lessonProvider = this.lessonProvider;
        lesson.chapterProvider = this.chapterProvider;
        lesson.userProvider = this.userProvider;
        lesson.courseProvider = this.courseProvider;
        return lesson;


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

    public Lesson save(long currentUserId) {


    }

    public void delete(long currentUserId) {


    }

    public Lesson get(long currentUserId) {


    }

    //generate LessonBuilder class
    public static class LessonBuilder {
        private long id;
        private String title;
        private String content;
        private String summary;
        private String thumbnail;
        private String  video;
        private String  free;
        private String  type;
        private long chapterId;
        private List<Resource> resources = new ArrayList<>();
        private LessonProvider lessonProvider;
        private ChapterProvider chapterProvider;
        private UserProvider userProvider;
        private CourseProvider courseProvider;

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

        public LessonBuilder free(String free) {
            this.free = free;
            return this;
        }

        public LessonBuilder type(String type) {
            this.type = type;
            return this;
        }

        public LessonBuilder chapterId(long chapterId) {
            this.chapterId = chapterId;
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

        public Lesson build() {
            return new Lesson(this);
        }
    }

}
