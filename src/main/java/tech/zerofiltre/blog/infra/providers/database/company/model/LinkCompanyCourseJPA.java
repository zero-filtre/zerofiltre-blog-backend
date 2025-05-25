package tech.zerofiltre.blog.infra.providers.database.company.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_course", uniqueConstraints = {@UniqueConstraint(name = "UniqueCompanyIdAndCourseId", columnNames = {"company_id", "course_id"})})
public class LinkCompanyCourseJPA extends BaseEntityJPA {

    @Column(name = "company_id")
    private long companyId;

    @Column(name = "course_id")
    private long courseId;

    private boolean exclusive;
    private boolean active;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

}
