package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.LessonJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class DBLessonProvider implements LessonProvider {

    private final LessonJPARepository lessonJPARepository;
    private final EnrollmentJPARepository enrollmentJPARepository;
    LessonJPAMapper lessonJPAMapper = Mappers.getMapper(LessonJPAMapper.class);

    @Override
    public Optional<Lesson> lessonOfId(long id) {
        return lessonJPARepository.findById(id).map(lessonJPAMapper::fromJPA);
    }

    @Override
    @CacheEvict(value = {"search-results"}, allEntries = true)
    public Lesson save(Lesson lesson) {
        LessonJPA lessonJPA = lessonJPAMapper.toJPA(lesson);
        LessonJPA saved = lessonJPARepository.save(lessonJPA);
        return lessonJPAMapper.fromJPA(saved);
    }

    @Override
    @CacheEvict(value = {"search-results"}, allEntries = true)
    public void delete(Lesson lesson) {
        enrollmentJPARepository.getAllByCompletedLessonsLesson(lessonJPAMapper.toJPA(lesson)).forEach(enrollmentJPA -> {
            enrollmentJPA.getCompletedLessons().removeIf(completedLessonJPA -> completedLessonJPA.getLesson().getId() == lesson.getId());
            enrollmentJPARepository.save(enrollmentJPA);
        });
        lessonJPARepository.delete(lessonJPAMapper.toJPA(lesson));
    }

    @Override
    public List<Long> listNotCompletedLessons(long enrollmentId) {
        return lessonJPARepository.findAllLessonIdNotCompletedByCourseIdAndEnrollmentId(enrollmentId);
    }

    @Override
    @CacheEvict(value = {"search-results"}, allEntries = true)
    public List<Lesson> saveAll(List<Lesson> lessons) {
        List<LessonJPA> lessonsJPA = lessonJPAMapper.toJPAs(lessons);
        List<LessonJPA> savedLessonsJPA = lessonJPARepository.saveAll(lessonsJPA);
        return lessonJPAMapper.fromJPAs(savedLessonsJPA);

    }

}
