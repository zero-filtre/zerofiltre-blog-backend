package tech.zerofiltre.blog.infra.entrypoints.rest.course.model;

import lombok.*;

import javax.validation.constraints.*;

@Data
@ToString
public class SectionVM {

    private long id;

    @Min(value = 1, message = "The position must not be zero")
    private int position;

    @NotNull(message = "The title must not be null")
    @NotEmpty(message = "The title must not be empty")
    private String title;
    @NotNull(message = "The content must not be null")
    @NotEmpty(message = "The content must not be empty")
    private String content;


    private String image;

    @Min(value = 1, message = "The course id must not be zero")
    private long courseId;
}
