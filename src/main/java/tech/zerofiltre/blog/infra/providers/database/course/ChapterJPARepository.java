package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

public interface ChapterJPARepository extends JpaRepository<ChapterJPA, Long> {
    List<ChapterJPA> findAllByCourseIdOrderByNumberAsc(long courseId);
}

