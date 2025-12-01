package tech.zerofiltre.blog.infra.providers.database.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.CompanyCourse;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyCourseJPA;

@Mapper
public interface CompanyCourse2JPAMapper {
    
    CompanyCourse fromJPA(CompanyCourseJPA companyCourseJPA);

}
