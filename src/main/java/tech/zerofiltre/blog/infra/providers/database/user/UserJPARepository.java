package tech.zerofiltre.blog.infra.providers.database.user;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import java.util.List;
import java.util.Optional;

public interface UserJPARepository extends JpaRepository<UserJPA, Long> {
    Optional<UserJPA> findByEmail(String email);

    List<UserJPA> findByIsActiveIsFalse();

    Optional<UserJPA> findBySocialId(String userSocialId);
}
