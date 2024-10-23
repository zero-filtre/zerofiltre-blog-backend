package tech.zerofiltre.blog.infra.providers.database.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyCourseJPA;

@Mapper
public interface CompanyCourseJPAMapper {
    
    CompanyCourseJPA toJPA(LinkCompanyCourse linkCompanyCourse);

    LinkCompanyCourse fromJPA(CompanyCourseJPA companyCourseJPA);

}
