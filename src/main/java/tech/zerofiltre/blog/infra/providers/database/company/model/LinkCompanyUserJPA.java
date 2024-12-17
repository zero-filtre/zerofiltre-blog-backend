package tech.zerofiltre.blog.infra.providers.database.company.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_user")
@IdClass(CompanyUserId.class)
public class LinkCompanyUserJPA {

    @Id
    @Column(name = "company_id")
    private long companyId;

    @Id
    @Column(name = "user_id")
    private long userId;

    private String role;

}
