package tech.zerofiltre.blog.infra.entrypoints.rest.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.zerofiltre.blog.domain.article.model.Tag;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PublishOrSaveCourseVM {
    @Min(1)
    private long id;

    @NotNull(message = "The subTitle must not be null")
    @NotEmpty(message = "The subTitle must not be empty")
    private String subTitle;

    @NotNull(message = "The summary must not be null")
    @NotEmpty(message = "The summary must not be empty")
    @Size(min = 20, max = 255, message = "The summary length should be between 50 and 255")
    private String summary;

    private String thumbnail;

    private List<Tag> tags = new ArrayList<>();

    @NotNull(message = "The title must not be null")
    @NotEmpty(message = "The title must not be empty")
    private String title;

    private String video;

    private List<SectionVM> sections = new ArrayList<>();

}
