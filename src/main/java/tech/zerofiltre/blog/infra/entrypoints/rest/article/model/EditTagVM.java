package tech.zerofiltre.blog.infra.entrypoints.rest.article.model;

import lombok.*;

import javax.validation.constraints.*;

@Data
public class EditTagVM {

    private long id;

    @NotNull(message = "The name must not be null")
    @NotEmpty(message = "The name must not be empty")
    private String name;

    @NotNull(message = "The color code must not be null")
    @NotEmpty(message = "The color code must not be empty")
    private String colorCode;
}
