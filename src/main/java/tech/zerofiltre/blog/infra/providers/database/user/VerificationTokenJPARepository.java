package tech.zerofiltre.blog.infra.providers.database.user;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.util.*;

public interface VerificationTokenJPARepository extends JpaRepository<VerificationTokenJPA, Long> {

    Optional<VerificationTokenJPA> findByToken(String token);

    Optional<VerificationTokenJPA> findByUser(UserJPA user);

    Optional<VerificationTokenJPA> findByUserEmail(String email);
}
