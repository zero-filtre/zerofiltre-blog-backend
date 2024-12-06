package tech.zerofiltre.blog.infra.providers.database.company;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyJPA;

public interface CompanyJPARepository extends JpaRepository<CompanyJPA, Long> {

}
