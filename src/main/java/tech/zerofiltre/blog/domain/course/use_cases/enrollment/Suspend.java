package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import lombok.extern.slf4j.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

@Slf4j
public class Suspend {
    private final EnrollmentProvider enrollmentProvider;
    private final CourseProvider courseProvider;
    private final ChapterProvider chapterProvider;

    public Suspend(EnrollmentProvider enrollmentProvider, CourseProvider courseProvider, ChapterProvider chapterProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.courseProvider = courseProvider;
        this.chapterProvider = chapterProvider;
    }

    public Enrollment execute(long userId, long courseId) throws BlogException {
        Enrollment enrollment = enrollmentProvider.enrollmentOf(userId, courseId, true)
                .orElseThrow(() -> new ForbiddenActionException("You are not enrolled in the course of id " + courseId, Domains.COURSE.name()));
        return doSuspend(userId, enrollment);
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }


    private int getEnrolledCount(long courseId) {
        return courseProvider.getEnrolledCount(courseId);
    }

    public void all(long userId, User.Plan relatedPlan) throws BlogException {
        List<Enrollment> enrollments = enrollmentProvider.of(0, Integer.MAX_VALUE, userId, null, null).getContent();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.isActive() && enrollment.getPlan().equals(relatedPlan)) {
                doSuspend(userId, enrollment);
            }
        }
    }

    private Enrollment doSuspend(long userId, Enrollment enrollment) throws BlogException {
        enrollment.setActive(false);
        enrollment.setSuspendedAt(LocalDateTime.now());
        Enrollment result = enrollmentProvider.save(enrollment);

        Course resultCourse = result.getCourse();
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        log.info("User {} enrollment suspended for course {}", userId, enrollment.getCourse().getId());
        return result;
    }
}
