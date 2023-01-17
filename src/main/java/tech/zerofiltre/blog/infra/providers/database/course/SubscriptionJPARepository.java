package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

public interface SubscriptionJPARepository extends JpaRepository<SubscriptionJPA, Long> {

    Optional<SubscriptionJPA> findBySubscriberIdAndCourseId(long subscriberId, long courseId);

    List<SubscriptionJPA> findBySubscriberId(long subscriberId);

    void deleteBySubscriberIdAndCourseId(long userId, long courseId);
}

