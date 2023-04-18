package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class NotFoundEnrollmentProviderDummy implements EnrollmentProvider {

    public boolean saveCalled;

    @Override
    public Enrollment save(Enrollment enrollment) {
        saveCalled = true;
        enrollment.setId(1);
        return enrollment;
    }

    @Override
    public void delete(long userId, long courseId) {

    }

    @Override
    public Page<Enrollment> of(int pageNumber, int pageSize, long authorId, FinderRequest.Filter filter, String tag) {
        return null;
    }


    @Override
    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        return Optional.empty();
    }
}
