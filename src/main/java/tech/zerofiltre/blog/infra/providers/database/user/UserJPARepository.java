package tech.zerofiltre.blog.infra.providers.database.user;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.util.*;

public interface UserJPARepository extends JpaRepository<UserJPA, Long> {
    Optional<UserJPA> findByEmail(String email);
}
