package tech.zerofiltre.blog.infra.entrypoints.rest.course.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.UpdateLessonVM;

@Mapper
public interface UpdateLessonVMMapper {

    Lesson fromVM(UpdateLessonVM updateLessonVM);

}
