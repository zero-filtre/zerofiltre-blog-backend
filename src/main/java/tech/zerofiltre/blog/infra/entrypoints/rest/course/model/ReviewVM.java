package tech.zerofiltre.blog.infra.entrypoints.rest.course.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.*;

@Data
@ToString
public class ReviewVM {
    @NotBlank
    private String chapterExplanations;

    @Min(1)
    @Max(5)
    private int chapterSatisfactionScore;

    @Min(1)
    @Max(5)
    private int chapterUnderstandingScore;

    private boolean recommendCourse;

    @Min(1)
    @Max(5)
    private int overallChapterSatisfaction;

    @NotNull
    @Positive(message = "The chapter id must be greater than 0")
    private long chapterId;
}
