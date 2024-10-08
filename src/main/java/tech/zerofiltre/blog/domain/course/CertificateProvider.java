package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;


public interface CertificateProvider {

    Certificate save(Certificate certificate);

    void delete(Certificate certificate);

    Certificate generate(User user, long courseId) throws ZerofiltreException;

    Optional<Certificate> findByUuid(String uuid) throws ZerofiltreException;

}
