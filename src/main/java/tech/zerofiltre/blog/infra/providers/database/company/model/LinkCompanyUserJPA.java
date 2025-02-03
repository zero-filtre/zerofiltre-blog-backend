package tech.zerofiltre.blog.infra.providers.database.company.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_user", uniqueConstraints = {@UniqueConstraint(name = "UniqueCompanyIdAndUserId", columnNames = {"company_id", "user_id"})})
public class LinkCompanyUserJPA extends BaseEntityJPA {

    @Column(name = "company_id")
    private long companyId;

    @Column(name = "user_id")
    private long userId;

    private String role;

    private boolean active;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

}
