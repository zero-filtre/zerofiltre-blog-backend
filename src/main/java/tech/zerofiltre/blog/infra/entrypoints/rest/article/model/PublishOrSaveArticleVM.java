package tech.zerofiltre.blog.infra.entrypoints.rest.article.model;

import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;

import javax.validation.constraints.*;
import java.util.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PublishOrSaveArticleVM {
    private long id;

    @NotNull(message = "The title must not be null")
    @NotEmpty(message = "The title must not be empty")
    private String title;

    private String thumbnail;

    @NotNull(message = "The summary must not be null")
    @NotEmpty(message = "The summary must not be empty")
    @Size(min = 20, max = 255, message = "The summary length should be between 50 and 255")
    private String summary;

    @NotNull(message = "The content must not be null")
    @NotEmpty(message = "The content must not be empty")
    private String content;
    private List<Tag> tags = new ArrayList<>();
}
