package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import org.springframework.boot.web.error.*;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.web.context.request.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.error.*;

import java.util.*;

class BlogErrorAttributes extends DefaultErrorAttributes {
    private final String currentApiVersion;

    public BlogErrorAttributes(final String currentApiVersion) {
        this.currentApiVersion = currentApiVersion;
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        final Map<String, Object> defaultErrorAttributes = super.getErrorAttributes(webRequest, options);
        final BlogError blogError = BlogError.fromDefaultAttributeMap(
                currentApiVersion, defaultErrorAttributes
        );
        return blogError.toAttributeMap();
    }

}
