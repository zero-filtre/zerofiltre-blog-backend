package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;

import java.util.List;

public interface CourseJPARepository extends JpaRepository<CourseJPA, Long> {

    Page<CourseJPA> findByStatus(Pageable pageable, Status status);

    Page<CourseJPA> findByStatusAndAuthorId(Pageable pageable, Status status, long authorId);

    List<CourseJPA> findByAuthorId(long userId);

    @Query("select a from CourseJPA a WHERE a.status=?1 ORDER BY size(a.reactions) desc ")
    Page<CourseJPA> findByReactionsDesc(Pageable pageable, Status status);

    @Query("select a from CourseJPA a WHERE a.status=?1 ORDER BY enrolledCount desc ")
    Page<CourseJPA> findByEnrolledDesc(Pageable pageable, Status status);

    Page<CourseJPA> findByStatusAndTagsName(Pageable pageable, Status status, String tag);

    Page<CourseJPA> findByStatusAndAuthorIdAndTagsName(Pageable pageable, Status status, long authorId, String tagName);

    @Query("select a from CourseJPA a WHERE a.status=?1 AND a.author.id=?2 ORDER BY size(a.reactions) desc ")
    Page<CourseJPA> findByReactionsAndAuthorIdDesc(Pageable pageable, Status status, long authorId);

    @Query("select a from CourseJPA a WHERE a.status=?1 AND a.author.id=?2 ORDER BY enrolledCount desc ")
    Page<CourseJPA> findByEnrolledAndAuthorIdDesc(Pageable pageable, Status status, long authorId);

    @Query("select count(a) from CourseJPA a JOIN EnrollmentJPA s ON a.id=s.course.id WHERE a.id=?1")
    int getEnrolledCount(long courseId);
}

