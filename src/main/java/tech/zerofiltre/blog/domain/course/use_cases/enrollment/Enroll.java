package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import lombok.extern.slf4j.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

@Slf4j
public class Enroll {

    private final EnrollmentProvider enrollmentProvider;
    private final CourseProvider courseProvider;
    private final UserProvider userProvider;
    private final ChapterProvider chapterProvider;

    public Enroll(EnrollmentProvider enrollmentProvider, CourseProvider courseProvider, UserProvider userProvider, ChapterProvider chapterProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.courseProvider = courseProvider;
        this.userProvider = userProvider;
        this.chapterProvider = chapterProvider;
    }

    public Enrollment execute(long userId, long courseId) throws BlogException {

        User user = userProvider.userOfId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We could not find the user with id " + userId,
                        String.valueOf(userId),
                        Domains.USER.name()));

        if (!user.isAdmin() && !user.isPro())
            throw new ForbiddenActionException("You must be a PRO to enroll to a course this way", Domains.COURSE.name());

        Course course = courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We couldn't find the course of id " + courseId + " you are trying to enroll to",
                        String.valueOf(courseId),
                        Domains.COURSE.name()));

        if (course.getStatus().compareTo(Status.PUBLISHED) < 0)
            throw new ForbiddenActionException("You can not get enrolled into an unpublished course", Domains.COURSE.name());

        Enrollment enrollment = new Enrollment();
        LocalDateTime lastModifiedAt = LocalDateTime.now();
        Optional<Enrollment> existingEnrollment = enrollmentProvider.enrollmentOf(userId, courseId, true);
        if (existingEnrollment.isPresent()) return existingEnrollment.get();

        Optional<Enrollment> cancelledEnrollment = enrollmentProvider.enrollmentOf(userId, courseId, false);
        if (cancelledEnrollment.isPresent()) {
            enrollment = cancelledEnrollment.get();
            enrollment.setActive(true);
            enrollment.setSuspendedAt(null);
        } else {
            enrollment.setCourse(course);
            enrollment.setUser(user);
            lastModifiedAt = enrollment.getEnrolledAt();
        }
        enrollment.setLastModifiedAt(lastModifiedAt);
        if(user.isPro()) enrollment.setPlan(User.Plan.PRO);
        Enrollment result = enrollmentProvider.save(enrollment);
        Course resultCourse = result.getCourse();
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        log.info("User {} enrolled to course {}", userId, courseId);
        return result;
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }


    private int getEnrolledCount(long courseId) {
        return courseProvider.getEnrolledCount(courseId);
    }
}
