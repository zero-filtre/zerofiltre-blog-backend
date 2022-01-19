package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.error.*;

import java.util.*;

import static tech.zerofiltre.blog.infra.entrypoints.rest.config.ErrorCodes.*;

@RestControllerAdvice
public class BlogControllerAdvice {

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<BlogError> handleArticleNotFoundException(ArticleNotFoundException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.NOT_FOUND.value()),
                ZBLOG_003.name(),
                messageSource.getMessage(ZBLOG_003.name(), new Object[]{exception.getArticleId()}, locale),
                "Article",
                exception.getLocalizedMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PublishOrSaveArticleException.class)
    public ResponseEntity<BlogError> handlePublishOrSaveException(PublishOrSaveArticleException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                ZBLOG_001.name(),
                messageSource.getMessage(ZBLOG_001.name(), new Object[]{exception.getItemId()}, locale),
                "Article",
                exception.getLocalizedMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<BlogError> handleGenericProblem(Throwable throwable, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                ZBLOG_000.name(),
                messageSource.getMessage(ZBLOG_000.name(), new Object[]{}, locale),
                "No domain available",
                throwable.getLocalizedMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
