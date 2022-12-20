package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBCourseProvider implements CourseProvider {

    private final CourseJPARepository repository;
    private final CourseJPAMapper mapper = Mappers.getMapper(CourseJPAMapper.class);
    private final SpringPageMapper<Course> pageMapper = new SpringPageMapper<>();


    @Override
    public Optional<Course> courseOfId(long id) {
        return repository.findById(id)
                .map(mapper::fromJPA);
    }

    @Override
    public Course save(Course course) {
        return mapper.fromJPA(repository.save(mapper.toJPA(course)));
    }

    @Override
    public void delete(Course existingCourse) {
        repository.delete(mapper.toJPA(existingCourse));
    }

    @Override
    public Page<Course> courseOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag) {
        org.springframework.data.domain.Page<CourseJPA> page;

        final var publishedAtPropertyName = "publishedAt";
        if (authorId == 0) {
            if (tag != null)
                page = repository.findByStatusAndTagsName(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, tag);
            else if (FinderRequest.Filter.POPULAR == filter)
                page = repository.findByReactionsDesc(PageRequest.of(pageNumber, pageSize), status);
            else if (FinderRequest.Filter.MOST_VIEWED == filter)
                page = repository.findByEnrolledDesc(PageRequest.of(pageNumber, pageSize), status);
            else
                page = repository.findByStatus(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status);
        } else {
            if (tag != null)
                page = repository.findByStatusAndAuthorIdAndTagsName(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, authorId, tag);
            else if (FinderRequest.Filter.POPULAR == filter)
                page = repository.findByReactionsAndAuthorIdDesc(PageRequest.of(pageNumber, pageSize), status, authorId);
            else if (FinderRequest.Filter.MOST_VIEWED == filter)
                page = repository.findByEnrolledAndAuthorIdDesc(PageRequest.of(pageNumber, pageSize), status, authorId);
            else
                page = repository.findByStatusAndAuthorId(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, authorId);
        }
        return pageMapper.fromSpringPage(page.map(mapper::fromJPA));
    }
}
