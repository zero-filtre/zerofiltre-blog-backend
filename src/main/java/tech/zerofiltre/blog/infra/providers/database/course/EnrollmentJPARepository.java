package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

public interface EnrollmentJPARepository extends JpaRepository<EnrollmentJPA, Long> {

    Optional<EnrollmentJPA> findByUserIdAndCourseIdAndActive(long userId, long courseId, boolean isActive);

    Page<EnrollmentJPA> findByUserIdAndActiveAndCompleted(Pageable pageable, long userId, boolean isActive, boolean isCompleted);

    void deleteByUserIdAndCourseId(long userId, long courseId);

    List<EnrollmentJPA> getAllByCompletedLessonsLesson(LessonJPA lessonJPA);
}

