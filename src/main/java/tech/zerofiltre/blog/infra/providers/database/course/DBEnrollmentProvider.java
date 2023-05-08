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
public class DBEnrollmentProvider implements EnrollmentProvider {

    private final EnrollmentJPARepository repository;
    private final EnrollmentJPAMapper mapper = Mappers.getMapper(EnrollmentJPAMapper.class);
    private final SpringPageMapper<Enrollment> pageMapper = new SpringPageMapper<>();


    @Override
    public void delete(long userId, long courseId) {
        repository.deleteByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag) {
        org.springframework.data.domain.Page<EnrollmentJPA> page
                = repository.findByUserIdAndActiveAndCompleted(
                PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "enrolledAt"),
                userId,
                !FinderRequest.Filter.INACTIVE.equals(filter),
                FinderRequest.Filter.COMPLETED.equals(filter));
        return pageMapper.fromSpringPage(page.map(mapper::fromJPA));

    }

    @Override
    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        return repository.findByUserIdAndCourseIdAndActive(userId, courseId, isActive)
                .map(mapper::fromJPA);
    }

    @Override
    public Enrollment save(Enrollment enrollment) throws ZerofiltreException {
        try {
            return mapper.fromJPA(repository.save(mapper.toJPA(enrollment)));
        } catch (DataIntegrityViolationException e) {
            throw new ZerofiltreException("You are already enrolled", e, "");
        }
    }

}
