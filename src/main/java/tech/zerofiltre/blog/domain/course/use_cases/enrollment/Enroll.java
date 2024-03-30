package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import lombok.extern.slf4j.Slf4j;
import tech.zerofiltre.blog.domain.Domains;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Resource;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tech.zerofiltre.blog.domain.sandbox.model.Sandbox.Type.K8S;

@Slf4j
public class Enroll {

    private final EnrollmentProvider enrollmentProvider;
    private final CourseProvider courseProvider;
    private final UserProvider userProvider;
    private final ChapterProvider chapterProvider;
    private final SandboxProvider sandboxProvider;
    private final PurchaseProvider purchaseProvider;

    public Enroll(EnrollmentProvider enrollmentProvider, CourseProvider courseProvider, UserProvider userProvider, ChapterProvider chapterProvider, SandboxProvider sandboxProvider, PurchaseProvider purchaseProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.courseProvider = courseProvider;
        this.userProvider = userProvider;
        this.chapterProvider = chapterProvider;
        this.sandboxProvider = sandboxProvider;
        this.purchaseProvider = purchaseProvider;
    }

    private static void checkIfCourseIsPublished(Course course) throws ForbiddenActionException {
        if (course.getStatus().compareTo(Status.PUBLISHED) < 0) {
            throw new ForbiddenActionException("You can not get enrolled into an unpublished course", Domains.COURSE.name());
        }
    }

    public Enrollment execute(long userId, long courseId, boolean fromEndUser) throws ZerofiltreException {

        User user = getTheUser(userId);
        //TODO Add purchase check condition here (bootcamp or not)
        Course course = getTheCourse(courseId);
        checkIfCanEnrollAsPro(fromEndUser, user, course);
        checkIfCourseIsPublished(course);
        //TODO When purchase condition check will be added few lines upper, there will be no need to check here
        checkIfMentoredCourseAndPurchased(userId, courseId, user, course);

        Enrollment existingEnrollment = getExisting(userId, courseId);
        if (existingEnrollment != null) return existingEnrollment;

        Enrollment enrollment = buildAndSaveEnrollment(userId, courseId, course, user);

        Course resultCourse = enrollment.getCourse();
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        initSandbox(resultCourse, user);
        log.info("User {} enrolled to course {}", userId, courseId);
        return enrollment;
    }

    private void initSandbox(Course resultCourse, User user) {
        Thread sandboxProvisioner = new Thread(() -> {
            if (sandboxProvider != null && K8S.equals(resultCourse.getSandboxType())) {
                try {
                    sandboxProvider.initialize(user.getFullName(), ZerofiltreUtils.getValidEmail(user));
                } catch (ZerofiltreException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "sandbox-provisioner");
        sandboxProvisioner.start();
    }

    private Enrollment buildAndSaveEnrollment(long userId, long courseId, Course course, User user) throws ZerofiltreException {
        Enrollment enrollment = new Enrollment();
        LocalDateTime lastModifiedAt = LocalDateTime.now();
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

        if (user.isPro() && !course.isMentored()) enrollment.setPlan(User.Plan.PRO);
        return enrollmentProvider.save(enrollment);
    }

    private Enrollment getExisting(long userId, long courseId) {
        Optional<Enrollment> existingEnrollment = enrollmentProvider.enrollmentOf(userId, courseId, true);
        return existingEnrollment.orElse(null);
    }

    private User getTheUser(long userId) throws ResourceNotFoundException {
        return userProvider.userOfId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We could not find the user with id " + userId,
                        String.valueOf(userId),
                        Domains.USER.name()));
    }

    private void checkIfMentoredCourseAndPurchased(long userId, long courseId, User user, Course course) throws ForbiddenActionException {
        if (!user.isAdmin() && course.isMentored() && purchaseProvider != null && purchaseProvider.purchaseOf(userId, courseId).isEmpty()) {
            throw new ForbiddenActionException("You must purchase this course to enroll", Domains.COURSE.name());
        }
    }

    private Course getTheCourse(long courseId) throws ResourceNotFoundException {
        return courseProvider.courseOfId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We couldn't find the course of id " + courseId + " you are trying to enroll to",
                        String.valueOf(courseId),
                        Domains.COURSE.name()));
    }

    private void checkIfCanEnrollAsPro(boolean fromEndUser, User user, Course course) throws ForbiddenActionException {
        if (!user.isAdmin() && !user.isPro() && fromEndUser && !hasARelatedEnrollment(user, course)) {
            throw new ForbiddenActionException("You must be a PRO to enroll to a course this way", Domains.COURSE.name());
        }
    }

    private boolean hasARelatedEnrollment(User user, Course course) {
        final int ALL_ENROLLMENTS = Integer.MAX_VALUE;
        return enrollmentProvider.of(0, ALL_ENROLLMENTS, user.getId(), null, null)
                .getContent().stream()
                .map(Enrollment::getCourse)
                .flatMap(enrollmentCourse -> chapterProvider.ofCourseId(enrollmentCourse.getId()).stream())
                .flatMap(chapter -> chapter.getLessons().stream())
                .flatMap(lesson -> lesson.getResources().stream())
                .anyMatch(resource -> isRelatedToCourse(resource, course));
    }

    private boolean isRelatedToCourse(Resource resource, Course targetCourse) {
        return "course".equals(resource.getType()) && targetCourse.getId() == extractCourseId(resource.getUrl());
    }

    private long extractCourseId(String url) {
        String pattern = "/cours/(\\d+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            return 0;
        }
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }


    private int getEnrolledCount(long courseId) {
        return courseProvider.getEnrolledCount(courseId);
    }
}
