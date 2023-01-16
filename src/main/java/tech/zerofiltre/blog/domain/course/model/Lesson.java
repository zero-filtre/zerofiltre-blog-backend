package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.course.*;

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
    private List<Resource> resources;
    private LessonProvider lessonProvider;

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
    }

    public static LessonBuilder builder() {
        return new LessonBuilder();
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

        public Lesson build() {
            return new Lesson(this);
        }
    }

}
