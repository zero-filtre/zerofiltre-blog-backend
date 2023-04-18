package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class NotEnrolledEnrollmentProvider implements EnrollmentProvider {

    @Override
    public void delete(long userId, long courseId) {

    }

    @Override
    public Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag) {
        return new Page<>();
    }

    @Override
    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        return Optional.empty();
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        return null;
    }
}
