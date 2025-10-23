package tech.zerofiltre.blog.infra.entrypoints.rest.company.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.course.model.Section;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCourseVM {

    private long id;
    private long price;
    private String thumbnail;
    private String title;
    private String subTitle;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime lastSavedAt;
    private List<Tag> tags = new ArrayList<>();
    private long enrolledCount;
    private Status status = Status.DRAFT;
    private User author;
    private String video;
    private List<Section> sections = new ArrayList<>();
    private List<Reaction> reactions = new ArrayList<>();
    private int lessonsCount;
    private Sandbox.Type sandboxType = Sandbox.Type.NONE;
    private boolean mentored;

    private boolean exclusive;

}
