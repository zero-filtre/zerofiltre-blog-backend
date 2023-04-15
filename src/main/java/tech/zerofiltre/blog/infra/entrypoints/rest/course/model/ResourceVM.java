package tech.zerofiltre.blog.infra.entrypoints.rest.course.model;

import lombok.*;

import javax.validation.constraints.*;

@Data
public class ResourceVM {
    @NotEmpty(message = "The name must be 1 to 100 chars long")
    @Size(min = 1, max = 100)
    private String name;

    @NotNull(message = "The type must not be null")
    @NotEmpty(message = "The type must not be empty")
    @Pattern(regexp = "txt|doc|pdf|img",message = "The type must be txt,doc,pdf or img")
    private String type;

    @NotNull(message = "The url must not be null")
    @NotEmpty(message = "The url must not be empty")
    private String url;

    @Positive(message = "The lesson id must be greater than 0")
    private long lessonId;
}
