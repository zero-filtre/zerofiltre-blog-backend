package tech.zerofiltre.blog.infra.entrypoints.rest.course.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.ReviewVM;

@Mapper
public interface ReviewVMMapper {

    Review fromVM(ReviewVM reviewVM);

}
