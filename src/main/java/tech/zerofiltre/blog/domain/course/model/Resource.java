package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.*;
import tech.zerofiltre.blog.domain.course.*;

@JsonIgnoreProperties(value = "resourceProvider")
public class Resource {
    private long id;
    private String type;
    private String url;
    private String name;
    private long lessonId;
    private ResourceProvider resourceProvider;

    private Resource(ResourceBuilder resourceBuilder) {
        this.id = resourceBuilder.id;
        this.type = resourceBuilder.type;
        this.url = resourceBuilder.url;
        this.name = resourceBuilder.name;
        this.lessonId = resourceBuilder.lessonId;
        this.resourceProvider = resourceBuilder.resourceProvider;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public long getLessonId() {
        return lessonId;
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public static ResourceBuilder builder() {
        return new ResourceBuilder();
    }

    public static class ResourceBuilder {
        private long id;
        private String type;
        private String url;
        private String name;
        private long lessonId;
        private ResourceProvider resourceProvider;

        public ResourceBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ResourceBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ResourceBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ResourceBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ResourceBuilder resourceProvider(ResourceProvider resourceProvider) {
            this.resourceProvider = resourceProvider;
            return this;
        }

        public ResourceBuilder lessonId(long lessonId) {
            this.lessonId = lessonId;
            return this;
        }

        public Resource build() {
            return new Resource(this);
        }
    }
}
