package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;

import java.util.*;

public interface EnrollmentProvider {

    void delete(long userId, long courseId);

    Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag);

    Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive);

    Enrollment save(Enrollment enrollment) throws BlogException;

}
