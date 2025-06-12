package tech.zerofiltre.blog.infra.providers.database.company;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.infra.providers.database.SpringPageMapper;
import tech.zerofiltre.blog.infra.providers.database.company.mapper.CompanyUserJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.company.model.LinkCompanyUserJPA;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class DBCompanyUserProvider implements CompanyUserProvider {

    private final CompanyUserJPARepository repository;
    private final CompanyUserJPAMapper mapper = Mappers.getMapper(CompanyUserJPAMapper.class);
    private final SpringPageMapper<LinkCompanyUser> pageMapper = new SpringPageMapper<>();

    @Override
    public LinkCompanyUser save(LinkCompanyUser linkCompanyUser) {
        return mapper.fromJPA(repository.save(mapper.toJPA(linkCompanyUser)));
    }

    @Override
    public Optional<LinkCompanyUser> findByCompanyIdAndUserId(long companyId, long userId) {
        return repository.findByCompanyIdAndUserId(companyId, userId).map(mapper::fromJPA);
    }

    @Override
    public Optional<LinkCompanyUser> findByCompanyIdAndUserId(long companyId, long userId, boolean active) {
        return repository.findByCompanyIdAndUserIdAndActive(companyId, userId, active).map(mapper::fromJPA);
    }

    @Override
    public Page<LinkCompanyUser> findAllByCompanyId(int pageNumber, int pageSize, long companyId) {
        org.springframework.data.domain.Page<LinkCompanyUserJPA> pageJpa = repository.findAllByCompanyId(PageRequest.of(pageNumber, pageSize), companyId);
        return pageMapper.fromSpringPage(pageJpa.map(mapper::fromJPA));
    }

    @Override
    public List<LinkCompanyUser> findAllByCompanyId(long companyId) {
        return repository.findAllByCompanyId(companyId).stream().map(mapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public List<LinkCompanyUser> findAllByCompanyIdExceptAdminRole(long companyId) {
        return repository.findAllByCompanyIdAndRoleNot(companyId, "ADMIN").stream().map(mapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public List<LinkCompanyUser> findAllByUserIdAndActive(long userId) {
        return repository.findAllByUserIdAndActiveIsTrue(userId).stream().map(mapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public void delete(LinkCompanyUser linkCompanyUser) {
        repository.delete(mapper.toJPA(linkCompanyUser));
    }

    @Override
    public void deleteAllByCompanyId(long companyId) {
        repository.deleteAllByCompanyId(companyId);
    }

    @Override
    public void deleteAllByCompanyIdExceptAdminRole(long companyId) {
        repository.deleteAllByCompanyIdAndRoleNot(companyId, "ADMIN");
    }

    @Override
    public void deleteAllByUserId(long userId) {
        repository.deleteAllByUserId(userId);
    }
}
