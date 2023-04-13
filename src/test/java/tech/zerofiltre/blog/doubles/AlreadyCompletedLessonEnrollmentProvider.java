package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class AlreadyCompletedLessonEnrollmentProvider implements EnrollmentProvider {


    public void delete(long userId, long courseId) {

    }

    public Page<Enrollment> of(int pageNumber, int pageSize, long userId, FinderRequest.Filter filter, String tag) {
        return null;
    }

    public Optional<Enrollment> enrollmentOf(long userId, long courseId, boolean isActive) {
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setId(224);
        enrollment1.setCourse(new Course());
        Lesson lesson = new Lesson.LessonBuilder().id(3).build();
        enrollment1.getCompletedLessons().add(lesson);
        return Optional.of(enrollment1);
    }

    public Enrollment save(Enrollment enrollment) {
        return null;
    }
}
