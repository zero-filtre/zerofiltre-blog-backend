package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBLessonProvider implements LessonProvider {

    private final LessonJPARepository lessonJPARepository;
    private final LessonJPANumberRepository numberRepository;
    private final SubscriptionJPARepository subscriptionJPARepository;
    LessonJPAMapper lessonJPAMapper = Mappers.getMapper(LessonJPAMapper.class);

    @Override
    public Optional<Lesson> lessonOfId(long id) {
        return lessonJPARepository.findById(id).map(lessonJPAMapper::fromJPA);
    }

    @Override
    public Lesson save(Lesson lesson) {
        LessonJPA lessonJPA = lessonJPAMapper.toJPA(lesson);
        if (lesson.getNumber() == 0) {
            lessonJPA.setNumber(numberRepository.save(new LessonJPANumber()));
        }
        return lessonJPAMapper.fromJPA(lessonJPARepository.save(lessonJPA));
    }

    @Override
    public void delete(Lesson lesson) {
        subscriptionJPARepository.getAllByCompletedLessonsContains(lessonJPAMapper.toJPA(lesson)).forEach(subscriptionJPA -> {
            subscriptionJPA.getCompletedLessons().removeIf(lessonJPA -> lessonJPA.getId() == lesson.getId());
            subscriptionJPARepository.save(subscriptionJPA);
        });
        lessonJPARepository.delete(lessonJPAMapper.toJPA(lesson));
    }
}
