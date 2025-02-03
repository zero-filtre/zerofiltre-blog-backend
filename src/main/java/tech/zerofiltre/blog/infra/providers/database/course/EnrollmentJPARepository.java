package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.infra.providers.database.course.model.EnrollmentJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;

import java.util.List;
import java.util.Optional;

public interface EnrollmentJPARepository extends JpaRepository<EnrollmentJPA, Long> {

    Optional<EnrollmentJPA> findByUserIdAndCourseIdAndActive(long userId, long courseId, boolean isActive);

    Page<EnrollmentJPA> findByUserIdAndActiveAndCompleted(Pageable pageable, long userId, boolean isActive, boolean isCompleted);

    void deleteByUserIdAndCourseId(long userId, long courseId);

    List<EnrollmentJPA> getAllByCompletedLessonsLesson(LessonJPA lessonJPA);

    @Query(value = "select completed from enrollment where user_id = ?1 and course_id = ?2", nativeQuery = true)
    boolean getCompletedByUserIdAndCourseId(long userId, long courseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update EnrollmentJPA set certificatePath=?1 where user.id=?2 and course.id=?3")
    void updateCertificatePathByUserIdAndCourseId(String certificatePath, long userId, long courseId);

    Optional<EnrollmentJPA> findByUserIdAndCourseId(long userId, long courseId);

    Optional<EnrollmentJPA> findByCompanyCourseIdAndActive(long companyCourseId, boolean isActive);

    List<EnrollmentJPA> findAllByCompanyCourseIdAndActive(long companyCourseId, boolean isActive);

    List<EnrollmentJPA> findAllByCompanyUserIdAndActive(long companyCourseId, boolean isActive);

}

