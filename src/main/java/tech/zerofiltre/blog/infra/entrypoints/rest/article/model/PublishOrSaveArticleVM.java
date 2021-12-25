package tech.zerofiltre.blog.infra.entrypoints.rest.article.model;

import lombok.*;
import tech.zerofiltre.blog.domain.article.model.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishOrSaveArticleVM {
    private long id;
    private String title;
    private String thumbnail;
    private String content;
    private List<Tag> tags = new ArrayList<>();
}
