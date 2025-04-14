package tech.zerofiltre.blog.domain.course.features.enrollment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;

import java.time.LocalDateTime;
import java.util.List;

import static tech.zerofiltre.blog.domain.sandbox.model.Sandbox.Type.K8S;

@Slf4j
@Component
public class Suspend {
    private final EnrollmentProvider enrollmentProvider;
    private final ChapterProvider chapterProvider;
    private final PurchaseProvider purchaseProvider;
    private final SandboxProvider sandboxProvider;
    private final CourseProvider courseProvider;

    public Suspend(EnrollmentProvider enrollmentProvider, ChapterProvider chapterProvider, PurchaseProvider purchaseProvider, SandboxProvider sandboxProvider, CourseProvider courseProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.chapterProvider = chapterProvider;
        this.purchaseProvider = purchaseProvider;
        this.sandboxProvider = sandboxProvider;
        this.courseProvider = courseProvider;
    }

    public Enrollment execute(long userId, long courseId) throws ZerofiltreException {
        Enrollment enrollment = enrollmentProvider.enrollmentOf(userId, courseId, true)
                .orElseThrow(() -> new ForbiddenActionException("You are not enrolled in the course of id " + courseId));
        return doSuspend(userId, enrollment);
    }

    public void all(long userId, boolean enrolledForLife) throws ZerofiltreException {
        List<Enrollment> enrollments = enrollmentProvider.of(0, Integer.MAX_VALUE, userId, null, null).getContent();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.isActive()
                    && enrollment.isForLife() == enrolledForLife
                    && enrollment.getCompanyUserId() == 0
                    && enrollment.getCompanyCourseId() == 0) {
                doSuspend(userId, enrollment);
            }
        }
    }

    public void byLinkId(long linkId) throws ZerofiltreException {
        List<Enrollment> list = enrollmentProvider.findAll(linkId, true);

        for(Enrollment enrollment : list) {
            enrollment.setActive(false);
            enrollment.setSuspendedAt(LocalDateTime.now());
            Enrollment result = enrollmentProvider.save(enrollment);

            if (K8S.equals(result.getCourse().getSandboxType())) {
                sandboxProvider.destroy(result.getUser().getFullName(), result.getUser().getEmail());
            }
        }
        if(!list.isEmpty()) {
            Course course = list.get(0).getCourse();
            course.setEnrolledCount(courseProvider.getEnrolledCount(course.getId()));
            courseProvider.save(course);
        }
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }

    private Enrollment doSuspend(long userId, Enrollment enrollment) throws ZerofiltreException {
        enrollment.setActive(false);
        enrollment.setSuspendedAt(LocalDateTime.now());
        Enrollment result = enrollmentProvider.save(enrollment);

        Course resultCourse = result.getCourse();
        purchaseProvider.delete(userId, resultCourse.getId());
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        log.info("User {} enrollment suspended for course {}", userId, enrollment.getCourse().getId());


        if (K8S.equals(resultCourse.getSandboxType())) {
            sandboxProvider.destroy(result.getUser().getFullName(), result.getUser().getEmail());
        }
        return result;
    }

}
