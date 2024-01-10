package tech.zerofiltre.blog.infra.providers.database.course.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;
import tech.zerofiltre.blog.infra.providers.database.article.model.ReactionCourseJPA;
import tech.zerofiltre.blog.infra.providers.database.article.model.TagJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "course")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CourseJPA extends BaseEntityJPA {
    private String subTitle;
    private String summary;
    private String thumbnail;
    private String sandboxType;
    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "course_tag",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagJPA> tags;
    private long enrolledCount;
    private String title;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime lastSavedAt;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "author_id")
    private UserJPA author;
    private long price;
    private String video;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    private Set<SectionJPA> sections;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    private Set<ReactionCourseJPA> reactions;
}
