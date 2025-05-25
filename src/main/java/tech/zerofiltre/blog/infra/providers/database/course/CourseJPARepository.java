package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseJPARepository extends JpaRepository<CourseJPA, Long> {

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "WHERE a.status = ?1 \n" +
            "AND (l.id IS NULL OR l.exclusive = false)")
    Page<CourseJPA> findByStatus(Pageable pageable, Status status);

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "  AND a.author.id = ?2 \n" +
            "WHERE a.status = ?1 \n" +
            "AND (l.id IS NULL OR l.exclusive = false)")
    Page<CourseJPA> findByStatusAndAuthorId(Pageable pageable, Status status, long authorId);

    List<CourseJPA> findByAuthorId(long userId);

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "WHERE a.status = ?1 \n" +
            "AND (l.id IS NULL OR l.exclusive = false) ORDER BY size(a.reactions) desc ")
    Page<CourseJPA> findByReactionsDesc(Pageable pageable, Status status);

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "WHERE a.status = ?1 \n" +
            "AND (l.id IS NULL OR l.exclusive = false) ORDER BY a.enrolledCount desc ")
    Page<CourseJPA> findByEnrolledDesc(Pageable pageable, Status status);

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "JOIN a.tags t \n" +
            "WHERE a.status = ?1 \n" +
            "  AND t.name = ?2 \n" +
            "  AND (l.id IS NULL OR l.exclusive = false)")
    Page<CourseJPA> findByStatusAndTagsName(Pageable pageable, Status status, String tag);

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "JOIN a.tags t \n" +
            "WHERE a.status = ?1 \n" +
            "  AND a.author.id = ?2 \n" +
            "  AND t.name = ?3 \n" +
            "  AND (l.id IS NULL OR l.exclusive = false)")
    Page<CourseJPA> findByStatusAndAuthorIdAndTagsName(Pageable pageable, Status status, long authorId, String tagName);

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "WHERE a.status = ?1 \n" +
            "  AND a.author.id = ?2 \n" +
            "  AND (l.id IS NULL OR l.exclusive = false)\n" +
            "ORDER BY size(a.reactions) DESC ")
    Page<CourseJPA> findByReactionsAndAuthorIdDesc(Pageable pageable, Status status, long authorId);

    @Query("SELECT a \n" +
            "FROM CourseJPA a \n" +
            "LEFT JOIN LinkCompanyCourseJPA l \n" +
            "  ON a.id = l.courseId \n" +
            "WHERE a.status = ?1 \n" +
            "  AND a.author.id = ?2 \n" +
            "  AND (l.id IS NULL OR l.exclusive = false)\n" +
            "ORDER BY a.enrolledCount DESC\n ")
    Page<CourseJPA> findByEnrolledAndAuthorIdDesc(Pageable pageable, Status status, long authorId);

    @Query("select count(a) from CourseJPA a JOIN EnrollmentJPA s ON a.id=s.course.id WHERE a.id=?1")
    int getEnrolledCount(long courseId);

    @Query("SELECT a FROM CourseJPA a WHERE a.status = :status AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.subTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<CourseJPA> findByKeyword(String keyword, Status status);


    @Query("SELECT count(l.id) from CourseJPA co " +
            "join ChapterJPA ch on ch.course.id=co.id " +
            "join LessonJPA l on l.chapter.id=ch.id " +
            "where co.id=:courseId")
    int getLessonsCount(long courseId);

    @Query("SELECT ch.course from ChapterJPA ch where ch.id=:chapterId")
    Optional<CourseJPA> findByChapterId(long chapterId);

    @Query("select a.title from CourseJPA a WHERE a.id=?1")
    String getTitle(long courseId);

    @Query("SELECT a FROM CourseJPA a WHERE a.lastPublishedAt >= ?1 AND a.lastPublishedAt < ?2")
    List<CourseJPA> findNewCoursesBetween(LocalDateTime startDate, LocalDateTime endDate);
}

