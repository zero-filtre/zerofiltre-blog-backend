package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.error.*;

@RestControllerAdvice
public class BlogControllerAdvice {

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @ExceptionHandler(ArticleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<BlogError> handleFindArticleException(ArticleNotFoundException exception) {
        final BlogError error = new BlogError(
                currentApiVersion,
                Integer.toString(HttpStatus.NOT_FOUND.value()),
                exception.getMessage(),
                "Article",
                "Aucun article correspondant à l'id fourni: " + exception.getArticleId(),
                null,
                null
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
