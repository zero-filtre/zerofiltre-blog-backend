package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.course.*;

import java.util.*;

public class Chapter {
    private long id;
    private String title;
    private long courseId;
    private List<Lesson> lessons;
    private ChapterProvider chapterProvider;

    private Chapter(ChapterBuilder chapterBuilder) {
        this.id = chapterBuilder.id;
        this.title = chapterBuilder.title;
        this.courseId = chapterBuilder.courseId;
        this.lessons = chapterBuilder.lessons;
        this.chapterProvider = chapterBuilder.chapterProvider;
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

    public ChapterProvider getChapterProvider() {
        return chapterProvider;
    }

    public static ChapterBuilder builder() {
        return new ChapterBuilder();
    }

    public static class ChapterBuilder {
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

        public Chapter build() {
            return new Chapter(this);
        }
    }

}
