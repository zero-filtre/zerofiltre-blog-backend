package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonWithCourseIdJPA;

import java.util.List;

public interface LessonJPARepository extends JpaRepository<LessonJPA, Long> {

    @Query("select new tech.zerofiltre.blog.infra.providers.database.course.model.LessonWithCourseIdJPA(l, co.id) from LessonJPA l " +
            "join ChapterJPA c on c.id=l.chapter.id " +
            "join CourseJPA co on co.id = c.course.id " +
            "where co.status= :status and" +
            "(LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    List<LessonWithCourseIdJPA> findByKeyword(String keyword, Status status);

    @Query(value = "SELECT l.id FROM lesson l " +
            "JOIN chapter c ON c.id = l.chapter_id AND c.course_id = (SELECT erl.course_id FROM enrollment erl WHERE erl.id = ?1) " +
            "LEFT JOIN enrollment_completed_lessons ecl ON ecl.enrollment_id = ?1 AND ecl.lesson_id = l.id " +
            "WHERE ecl.lesson_id IS NULL", nativeQuery = true)
    List<Long> findAllLessonIdNotCompletedByCourseIdAndEnrollmentId(long enrollmentId);

    List<LessonJPA> findAllByChapterIdOrderByNumberAsc(long chapterId);
}