package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface SectionProvider {
    Optional<Section> findById(long id);
    Section save(Section section);

    void delete(Section section);
}

