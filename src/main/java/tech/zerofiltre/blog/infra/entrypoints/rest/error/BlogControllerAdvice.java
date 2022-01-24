package tech.zerofiltre.blog.infra.entrypoints.rest.error;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.springframework.http.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;

import java.util.*;

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
                "ZBLOG_003",
                messageSource.getMessage("ZBLOG_003", new Object[]{exception.getArticleId()}, locale),
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
                "ZBLOG_001",
                messageSource.getMessage("ZBLOG_001", new Object[]{exception.getItemId()}, locale),
                "Article",
                exception.getLocalizedMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BlogError> handleException(MethodArgumentNotValidException exception, Locale locale) {
        String errorMessage = exception.getAllErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce("", (prev, next) -> prev = prev + next + "; ");

        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "ZBLOG_004",
                messageSource.getMessage("ZBLOG_004", new Object[]{}, locale),
                "No domain available",
                errorMessage
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<BlogError> handleException(UserAlreadyExistException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "ZBLOG_005",
                messageSource.getMessage("ZBLOG_005", new Object[]{exception.getEmail()}, locale),
                "User",
                exception.getLocalizedMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BlogError> handleException(InvalidTokenException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "ZBLOG_006",
                messageSource.getMessage("ZBLOG_006", null, locale),
                "User",
                exception.getLocalizedMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<BlogError> handleGenericProblem(Throwable throwable, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "ZBLOG_000",
                messageSource.getMessage("ZBLOG_000", new Object[]{}, locale),
                "No domain available",
                throwable.getLocalizedMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
