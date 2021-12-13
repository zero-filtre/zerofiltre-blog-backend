package tech.zerofiltre.blog.infra.providers.database.user;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

public interface UserJPARepository extends JpaRepository<UserJPA, Long> {
}
