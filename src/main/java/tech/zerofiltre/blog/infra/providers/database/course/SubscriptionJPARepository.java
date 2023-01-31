package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

public interface SubscriptionJPARepository extends JpaRepository<SubscriptionJPA, Long> {

    Optional<SubscriptionJPA> findBySubscriberIdAndCourseIdAndActive(long subscriberId, long courseId, boolean isActive);

    Page<SubscriptionJPA> findBySubscriberIdAndActive(Pageable pageable, long subscriberId, boolean isActive);

    void deleteBySubscriberIdAndCourseId(long userId, long courseId);
}

