package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import lombok.extern.slf4j.Slf4j;
import tech.zerofiltre.blog.domain.Domains;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class Suspend {
    private final EnrollmentProvider enrollmentProvider;
    private final CourseProvider courseProvider;
    private final ChapterProvider chapterProvider;
    private final PurchaseProvider purchaseProvider;

    public Suspend(EnrollmentProvider enrollmentProvider, CourseProvider courseProvider, ChapterProvider chapterProvider, PurchaseProvider purchaseProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.courseProvider = courseProvider;
        this.chapterProvider = chapterProvider;
        this.purchaseProvider = purchaseProvider;
    }

    public Enrollment execute(long userId, long courseId) throws ZerofiltreException {
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

    public void all(long userId, User.Plan relatedPlan) throws ZerofiltreException {
        List<Enrollment> enrollments = enrollmentProvider.of(0, Integer.MAX_VALUE, userId, null, null).getContent();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.isActive() && enrollment.getPlan().equals(relatedPlan)) {
                doSuspend(userId, enrollment);
            }
        }
    }

    private Enrollment doSuspend(long userId, Enrollment enrollment) throws ZerofiltreException {
        enrollment.setActive(false);
        enrollment.setSuspendedAt(LocalDateTime.now());
        Enrollment result = enrollmentProvider.save(enrollment);

        Course resultCourse = result.getCourse();
        //TODO Delete purchase for classic course as well
        if (resultCourse.isMentored()) purchaseProvider.delete(userId, resultCourse.getId());
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        log.info("User {} enrollment suspended for course {}", userId, enrollment.getCourse().getId());
        return result;
    }
}
