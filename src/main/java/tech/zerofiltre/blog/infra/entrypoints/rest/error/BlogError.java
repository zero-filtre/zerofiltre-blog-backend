package tech.zerofiltre.blog.infra.entrypoints.rest.error;

import lombok.*;

@Getter
public class BlogError {

    private final String apiVersion;
    private final ErrorBlock error;

    public BlogError(final String apiVersion, final String status, final String code, final String message,
                     final String reason) {
        this.apiVersion = apiVersion;
        this.error = new ErrorBlock(status, code, message, reason);
    }

    @Getter
    @RequiredArgsConstructor
    private static final class ErrorBlock {

        private final String status;
        private final String code;
        private final String message;
        private final String reason;

    }


}
