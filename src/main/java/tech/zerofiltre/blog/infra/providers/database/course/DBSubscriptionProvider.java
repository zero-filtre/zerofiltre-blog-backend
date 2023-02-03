package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.dao.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBSubscriptionProvider implements SubscriptionProvider {

    private final SubscriptionJPARepository repository;
    private final SubscriptionJPAMapper mapper = Mappers.getMapper(SubscriptionJPAMapper.class);
    private final SpringPageMapper<Subscription> pageMapper = new SpringPageMapper<>();


    @Override
    public void delete(long userId, long courseId) {
        repository.deleteBySubscriberIdAndCourseId(userId, courseId);
    }

    @Override
    public Page<Subscription> of(int pageNumber, int pageSize, long subscriberId, FinderRequest.Filter filter, String tag) {
        org.springframework.data.domain.Page<SubscriptionJPA> page
                = repository.findBySubscriberIdAndActiveAndCompleted(
                PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "subscribedAt"),
                subscriberId,
                !FinderRequest.Filter.INACTIVE.equals(filter),
                FinderRequest.Filter.COMPLETED.equals(filter));
        return pageMapper.fromSpringPage(page.map(mapper::fromJPA));

    }

    @Override
    public Optional<Subscription> subscriptionOf(long userId, long courseId, boolean isActive) {
        return repository.findBySubscriberIdAndCourseIdAndActive(userId, courseId, isActive)
                .map(mapper::fromJPA);
    }

    @Override
    public Subscription save(Subscription subscription) throws BlogException {
        try {
            return mapper.fromJPA(repository.save(mapper.toJPA(subscription)));
        } catch (DataIntegrityViolationException e) {
            throw new BlogException("You are already subscribed", e, "");
        }
    }

}
