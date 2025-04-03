package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Enrollment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<Enrollment> enrollmentOf(long userId, long courseId) {
        return Optional.empty();
    }

    @Override
    public Optional<Enrollment> find(long companyCourseId, boolean isActive) {
        return Optional.empty();
    }

    @Override
    public List<Enrollment> findAll(long companyCourseId, boolean isActive) {
        return new ArrayList<>();
    }

    @Override
    public List<Enrollment> findAllByCompanyUserId(long companyCourseId, boolean isActive) {
        return List.of();
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        return null;
    }

    @Override
    public boolean isCompleted(long userId, long courseId) { return false; }

    @Override
    public void setCertificatePath(String path, long id, long courseId) {

    }

    @Override
    public Enrollment enrollmentOf(String uuid) {
        return null;
    }
}
