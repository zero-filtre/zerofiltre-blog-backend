package tech.zerofiltre.blog.infra.providers.database.company;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.infra.providers.database.SpringPageMapper;
import tech.zerofiltre.blog.infra.providers.database.company.mapper.CompanyJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyJPA;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class DBCompanyProvider implements CompanyProvider {
    private final CompanyJPARepository repository;
    private final CompanyJPAMapper mapper = Mappers.getMapper(CompanyJPAMapper.class);
    private final SpringPageMapper<Company> pageMapper = new SpringPageMapper<>();

    @Override
    public Company save(Company company) {
        return mapper.fromJPA(repository.save(mapper.toJPA(company)));
    }

    @Override
    public Optional<Company> findById(long id) {
        return repository.findById(id)
                .map(mapper::fromJPA);
    }

    @Override
    public Page<Company> findAll(int pageNumber, int pageSize) {
        org.springframework.data.domain.Page<CompanyJPA> pageJpa = repository.findAll(PageRequest.of(pageNumber, pageSize));
        return pageMapper.fromSpringPage(pageJpa.map(mapper::fromJPA));
    }

    @Override
    public Page<Company> findAllByUserId(int pageNumber, int pageSize, long userId) {
        org.springframework.data.domain.Page<CompanyJPA> pageJpa = repository.findAllByUserId(PageRequest.of(pageNumber, pageSize), userId);
        return pageMapper.fromSpringPage(pageJpa.map(mapper::fromJPA));
    }

    @Override
    public List<Long> findAllCompanyIdByUserIdAndCourseId(long userId, long courseId) {
        return repository.findAllCompanyIdByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public void delete(Company company) {
        repository.delete(mapper.toJPA(company));
    }

}
