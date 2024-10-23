package tech.zerofiltre.blog.infra.providers.database.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.infra.providers.database.company.model.LinkCompanyCourseJPA;

@Mapper
public interface CompanyCourseJPAMapper {
    
    LinkCompanyCourseJPA toJPA(LinkCompanyCourse linkCompanyCourse);

    LinkCompanyCourse fromJPA(LinkCompanyCourseJPA linkCompanyCourseJPA);

}
