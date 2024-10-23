package tech.zerofiltre.blog.infra.providers.database.company.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "company")
public class CompanyJPA extends BaseEntityJPA {

    @Column(unique = true)
    private String companyName;

    @Column(unique = true, length = 9)
    private String siren;

}
