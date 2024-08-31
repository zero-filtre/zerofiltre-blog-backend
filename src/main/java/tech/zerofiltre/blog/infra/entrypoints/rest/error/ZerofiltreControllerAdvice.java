package tech.zerofiltre.blog.infra.entrypoints.rest.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tech.zerofiltre.blog.domain.article.use_cases.PublishOrSaveArticleException;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.user.use_cases.InvalidTokenException;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.servlet.ServletException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class ZerofiltreControllerAdvice {

    public static final String ZBLOG_000 = "ZBLOG_000";
    public static final String FULL_EXCEPTION = "Full exception";
    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BlogError> handleResourceNotFoundException(ResourceNotFoundException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.NOT_FOUND.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_003", new Object[]{exception.getResourceId()}, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PublishOrSaveArticleException.class)
    public ResponseEntity<BlogError> handlePublishOrSaveException(PublishOrSaveArticleException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_001", new Object[]{exception.getItemId()}, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<BlogError> handleException(MethodArgumentNotValidException exception, Locale locale) {
        String errorMessage = exception.getAllErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce("", (prev, next) -> prev = prev + next + "; ");

        String errorCode = UUID.randomUUID().toString();

        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_004", new Object[]{}, locale),
                errorMessage
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<BlogError> handleException(MissingServletRequestParameterException exception, Locale locale) {

        String errorCode = UUID.randomUUID().toString();

        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_004", new Object[]{}, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<BlogError> handleConstraintViolationException(ConstraintViolationException ex, Locale locale) {
        String errorMessage = ex.getConstraintViolations()
                .stream().map(ConstraintViolation::getMessage)
                .reduce("", (prev, next) -> prev = prev + next + "; ");

        String errorCode = UUID.randomUUID().toString();

        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_004", new Object[]{}, locale),
                errorMessage
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<BlogError> handleException(ResourceAlreadyExistException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.FORBIDDEN.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_005", new Object[]{exception.getUniqueIdentifier()}, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BlogError> handleException(InvalidTokenException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_006", null, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<BlogError> handleException(PaymentException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_011", null, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
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
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<BlogError> handleException(ForbiddenActionException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.FORBIDDEN.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_008", null, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnAuthenticatedActionException.class)
    public ResponseEntity<BlogError> handleException(UnAuthenticatedActionException exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.UNAUTHORIZED.value()),
                errorCode,
                messageSource.getMessage("ZBLOG_007", null, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageConversionException.class})
    public ResponseEntity<BlogError> handleException(Exception exception, Locale locale) {
        String errorCode = UUID.randomUUID().toString();
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                errorCode,
                messageSource.getMessage(ZBLOG_000, null, locale),
                ZerofiltreUtils.getRootCauseMessage(exception)
        );
        log.error(FULL_EXCEPTION + "-" + errorCode + ":", exception);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ZerofiltreException.class, ServletException.class, Throwable.class})
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
