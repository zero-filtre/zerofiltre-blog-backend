package tech.zerofiltre.blog.infra.entrypoints.rest.error;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.payment.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;

import javax.servlet.*;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class BlogControllerAdvice {

    public static final String ZBLOG_000 = "ZBLOG_000";
    public static final String FULL_EXCEPTION = "Full exception";
    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BlogError> handleResourceNotFoundException(ResourceNotFoundException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.NOT_FOUND.value()),
                "ZBLOG_003",
                messageSource.getMessage("ZBLOG_003", new Object[]{exception.getResourceId()}, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PublishOrSaveArticleException.class)
    public ResponseEntity<BlogError> handlePublishOrSaveException(PublishOrSaveArticleException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "ZBLOG_001",
                messageSource.getMessage("ZBLOG_001", new Object[]{exception.getItemId()}, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
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
                errorMessage
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<BlogError> handleException(ResourceAlreadyExistException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.FORBIDDEN.value()),
                "ZBLOG_005",
                messageSource.getMessage("ZBLOG_005", new Object[]{exception.getUniqueIdentifier()}, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BlogError> handleException(InvalidTokenException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "ZBLOG_006",
                messageSource.getMessage("ZBLOG_006", null, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<BlogError> handleException(PaymentException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "ZBLOG_011",
                messageSource.getMessage("ZBLOG_011", null, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VideoUploadFailedException.class)
    public ResponseEntity<BlogError> handleException(VideoUploadFailedException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_012", null, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<BlogError> handleException(ForbiddenActionException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.FORBIDDEN.value()),
                "ZBLOG_008",
                messageSource.getMessage("ZBLOG_008", null, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnAuthenticatedActionException.class)
    public ResponseEntity<BlogError> handleException(UnAuthenticatedActionException exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.UNAUTHORIZED.value()),
                "ZBLOG_007",
                messageSource.getMessage("ZBLOG_007", null, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageConversionException.class})
    public ResponseEntity<BlogError> handleException(Exception exception, Locale locale) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                ZBLOG_000,
                messageSource.getMessage(ZBLOG_000, null, locale),
                exception.getLocalizedMessage()
        );
        log.error(FULL_EXCEPTION, exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BlogException.class, ServletException.class, Throwable.class})
    public ResponseEntity<BlogError> handleGenericProblem(Throwable throwable, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                errorCode,
                messageSource.getMessage(ZBLOG_000, new Object[]{}, locale),
                "Unknown error, get help by providing the code: " + errorCode + " to the support team"
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", throwable);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
