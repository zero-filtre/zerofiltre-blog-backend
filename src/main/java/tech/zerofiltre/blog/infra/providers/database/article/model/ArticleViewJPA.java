package tech.zerofiltre.blog.infra.providers.database.article.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import javax.persistence.*;
import java.time.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "article_view")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class ArticleViewJPA extends BaseEntityJPA {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "viewer_id")
    private UserJPA viewer;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "viewed_id")
    private ArticleJPA viewed;


    private LocalDateTime viewedAt;
}
