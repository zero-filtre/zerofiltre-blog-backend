package tech.zerofiltre.blog.infra.entrypoints.rest.course.model;

import lombok.*;

import javax.validation.constraints.*;

@Data
@ToString
public class SaveChapterVM {

    private long id;

    @NotNull(message = "The title must not be null")
    @NotEmpty(message = "The title must not be empty")
    private String title;

    @Min(value = 1, message = "The course id must not be zero")
    private long courseId;
}
