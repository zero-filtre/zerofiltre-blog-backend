package tech.zerofiltre.blog.infra.providers.database.company;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.infra.providers.database.SpringPageMapper;
import tech.zerofiltre.blog.infra.providers.database.company.mapper.CompanyCourseJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.company.model.LinkCompanyCourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.CourseJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class DBCompanyCourseProvider implements CompanyCourseProvider {

    private final CompanyCourseJPARepository repository;
    private final CompanyCourseJPAMapper mapper = Mappers.getMapper(CompanyCourseJPAMapper.class);
    private final SpringPageMapper<LinkCompanyCourse> pageMapper = new SpringPageMapper<>();
    private final CourseJPAMapper courseMapper = Mappers.getMapper(CourseJPAMapper.class);
    private final SpringPageMapper<Course> pageCourseMapper = new SpringPageMapper<>();

    @Override
    public LinkCompanyCourse save(LinkCompanyCourse linkCompanyCourse) {
        return mapper.fromJPA(repository.save(mapper.toJPA(linkCompanyCourse)));
    }

    @Override
    public Optional<LinkCompanyCourse> findByCompanyIdAndCourseId(long companyId, long courseId) {
        return repository.findByCompanyIdAndCourseId(companyId, courseId).map(mapper::fromJPA);
    }

    @Override
    public Optional<LinkCompanyCourse> findByCompanyIdAndCourseId(long companyId, long courseId, boolean active) {
        return repository.findByCompanyIdAndCourseIdAndActive(companyId, courseId, active).map(mapper::fromJPA);
    }

    @Override
    public Page<LinkCompanyCourse> findByCompanyId(int pageNumber, int pageSize, long companyId) {
        org.springframework.data.domain.Page<LinkCompanyCourseJPA> pageJpa = repository.findAllByCompanyId(PageRequest.of(pageNumber, pageSize), companyId);
        return pageMapper.fromSpringPage(pageJpa.map(mapper::fromJPA));
    }

    @Override
    public Page<Course> findCoursesByCompanyId(int pageNumber, int pageSize, long companyId, Status status) {
        org.springframework.data.domain.Page<CourseJPA> pageJpa = repository.findCoursesByCompanyId(PageRequest.of(pageNumber, pageSize), companyId, status);
        return pageCourseMapper.fromSpringPage(pageJpa.map(courseMapper::fromJPA));
    }

    @Override
    public List<LinkCompanyCourse> findAllByCompanyId(long companyId) {
        return repository.findAllByCompanyId(companyId).stream().map(mapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public List<LinkCompanyCourse> findAllByCourseId(long courseId) {
        return repository.findAllByCourseId(courseId).stream().map(mapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public void delete(LinkCompanyCourse linkCompanyCourse) {
        repository.delete(mapper.toJPA(linkCompanyCourse));
    }

    @Override
    public void deleteAllByCompanyId(long companyId) {
        repository.deleteAllByCompanyId(companyId);
    }

    @Override
    public void deleteAllByCourseId(long courseId) {
        repository.deleteAllByCourseId(courseId);
    }

}
