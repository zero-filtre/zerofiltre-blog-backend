package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.providers.database.course.model.CertificateJPA;

import java.util.Optional;

@Repository
public interface CertificateJPARepository extends JpaRepository<CertificateJPA, Long> {

    Optional<CertificateJPA> findByUuid(String uuid) throws ZerofiltreException;

}
