package tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.zerofiltre.blog.domain.company.model.CompanyCourse;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.CompanyCourseVM;

import java.util.List;

@Mapper
public interface CompanyCourseVMMapper {

    @Mapping(target = ".", source = "course")
    @Mapping(source = "exclusive", target = "exclusive")
    CompanyCourseVM toVM(CompanyCourse companyCourse);

    List<CompanyCourseVM> toVMList(List<CompanyCourse> companyCourses);
}
