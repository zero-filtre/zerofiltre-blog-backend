package tech.zerofiltre.blog.domain.course.model;

import tech.zerofiltre.blog.domain.course.*;

public class Resource {
    private long id;
    private String type;
    private String url;
    private String name;
    private ResourceProvider resourceProvider;

    private Resource(ResourceBuilder resourceBuilder) {
        this.id = resourceBuilder.id;
        this.type = resourceBuilder.type;
        this.url = resourceBuilder.url;
        this.name = resourceBuilder.name;
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

        public Resource build() {
            return new Resource(this);
        }
    }
}
