package tech.zerofiltre.blog.infra.entrypoints.rest.course.model;

import lombok.*;

import javax.validation.constraints.*;

@Data
@ToString
public class SaveLessonVM {

    private long id;
    @NotNull(message = "The title must not be null")
    @NotEmpty(message = "The title must not be empty")
    private String title;
    @NotNull(message = "The content must not be null")
    @NotEmpty(message = "The content must not be empty")
    private String content;
    @NotNull(message = "The summary must not be null")
    @NotEmpty(message = "The summary must not be empty")
    @Size(min = 20, max = 255, message = "The summary length should be between 50 and 255")
    private String summary;
    private String thumbnail;
    private String video;
    private boolean free;
    private String type;
    private long chapterId;
}
