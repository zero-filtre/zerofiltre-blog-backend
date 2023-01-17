package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;

import java.util.*;
import java.util.stream.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBSubscriptionProvider implements SubscriptionProvider {

    private final SubscriptionJPARepository repository;
    private final SubscriptionJPAMapper mapper = Mappers.getMapper(SubscriptionJPAMapper.class);

    @Override
    public void delete(long userId, long courseId) {
        repository.deleteBySubscriberIdAndCourseId(userId, courseId);
    }

    @Override
    public List<Subscription> subscriptionsOf(long userId) {
        return repository.findBySubscriberId(userId)
                .stream()
                .map(mapper::fromJPA)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Subscription> subscriptionOf(long userId, long courseId) {
        return repository.findBySubscriberIdAndCourseId(userId, courseId)
                .map(mapper::fromJPA);
    }

    @Override
    public Subscription save(Subscription subscription) {
        return mapper.fromJPA(repository.save(mapper.toJPA(subscription)));
    }
}
