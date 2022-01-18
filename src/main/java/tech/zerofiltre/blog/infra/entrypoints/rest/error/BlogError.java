package tech.zerofiltre.blog.infra.entrypoints.rest.error;

import lombok.*;

import java.util.*;

@Getter
public class BlogError {

    private final String apiVersion;
    private final ErrorBlock error;

    public BlogError(final String apiVersion, final String code, final String message, final String domain,
                     final String reason) {
        this.apiVersion = apiVersion;
        this.error = new ErrorBlock(code, message, domain, reason);
    }

    public static BlogError fromDefaultAttributeMap(final String apiVersion,
                                                    final Map<String, Object> defaultErrorAttributes) {
        // original attribute values are documented in org.springframework.boot.web.servlet.error.DefaultErrorAttributes
        return new BlogError(
                apiVersion,
                ((Integer) defaultErrorAttributes.get("status")).toString(),
                (String) defaultErrorAttributes.getOrDefault("message", "no message available"),
                (String) defaultErrorAttributes.getOrDefault("path", "no domain available"),
                (String) defaultErrorAttributes.getOrDefault("error", "no reason available")
        );
    }

    // utility method to return a map of serialized root attributes,
    // see the last part of the guide for more details
    public Map<String, Object> toAttributeMap() {
        return Map.of(
                "apiVersion", apiVersion,
                "error", error
        );
    }

    @Getter
    @RequiredArgsConstructor
    private static final class ErrorBlock {

        private final String code;
        private final String message;
        private final String domain;
        private final String reason;

    }


}
