package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;

import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBLessonProvider implements LessonProvider {

    private final LessonJPARepository lessonJPARepository;
    LessonJPAMapper lessonJPAMapper = Mappers.getMapper(LessonJPAMapper.class);

    @Override
    public Optional<Lesson> lessonOfId(long id) {
        return lessonJPARepository.findById(id).map(lessonJPAMapper::fromJPA);
    }

    @Override
    public Lesson save(Lesson lesson) {
        return lessonJPAMapper.fromJPA(lessonJPARepository.save(lessonJPAMapper.toJPA(lesson)));
    }

    @Override
    public void delete(Lesson lesson) {
        lessonJPARepository.delete(lessonJPAMapper.toJPA(lesson));
    }
}
