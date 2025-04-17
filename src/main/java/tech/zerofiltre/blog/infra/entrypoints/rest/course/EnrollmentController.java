package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.features.enrollment.*;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.CertificateVerificationResponseVM;
import tech.zerofiltre.blog.util.DataChecker;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {

    private final SecurityContextManager securityContextManager;
    private final Enroll enroll;
    private final Suspend suspend;
    private final CompleteLesson completeLesson;
    private final FindEnrollment findEnrollment;
    private final CertificateService certificateService;
    private final MessageSource messageSource;
    private final CertificateProvider certificateProvider;
    private final DataChecker dataChecker;

    public EnrollmentController(
            EnrollmentProvider enrollmentProvider,
            CourseProvider courseProvider,
            UserProvider userProvider,
            SecurityContextManager securityContextManager,
            LessonProvider lessonProvider,
            ChapterProvider chapterProvider,
            SandboxProvider sandboxProvider,
            PurchaseProvider purchaseProvider,
            CertificateProvider certificateProvider,
            MessageSource messageSource,
            CompanyProvider companyProvider,
            CompanyCourseProvider companyCourseProvider,
            CompanyUserProvider companyUserProvider,
            DataChecker dataChecker) {
        this.securityContextManager = securityContextManager;
        this.messageSource = messageSource;
        this.certificateProvider = certificateProvider;
        this.dataChecker = dataChecker;
        enroll = new Enroll(enrollmentProvider, courseProvider, userProvider, chapterProvider, sandboxProvider, purchaseProvider, companyProvider, companyCourseProvider, companyUserProvider, dataChecker);
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider, courseProvider);
        completeLesson = new CompleteLesson(enrollmentProvider, lessonProvider, chapterProvider, courseProvider);
        findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
        certificateService = new CertificateService(enrollmentProvider, certificateProvider, messageSource);
        }

    @PostMapping
    public Enrollment enroll(@RequestParam long courseId, @RequestParam(required = false) Long companyId) throws ZerofiltreException {
        return enroll.execute(securityContextManager.getAuthenticatedUser().getId(), courseId, null == companyId ? 0 : companyId, false);
    }

    @DeleteMapping
    public void unEnroll(@RequestParam long courseId) throws ZerofiltreException {
        suspend.execute(securityContextManager.getAuthenticatedUser().getId(), courseId);
    }

    @PatchMapping("/complete")
    public Enrollment completeLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ZerofiltreException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), true);
    }

    @PatchMapping("/uncomplete")
    public Enrollment unCompleteLesson(@RequestParam long lessonId, @RequestParam long courseId) throws ZerofiltreException {
        return completeLesson.execute(courseId, lessonId, securityContextManager.getAuthenticatedUser().getId(), false);
    }

    @GetMapping
    public Enrollment getEnrollment(@RequestParam long courseId, @RequestParam long userId) throws ResourceNotFoundException, ForbiddenActionException {
        User executor = securityContextManager.getAuthenticatedUser();
        return findEnrollment.of(courseId, userId, executor.getId(), executor.isAdmin());
    }

    @GetMapping("/user")
    Page<Course> coursesOfEnrollment(
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String tag) throws UserNotFoundException {
        FinderRequest request = new FinderRequest();
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setUser(securityContextManager.getAuthenticatedUser());
        request.setTag(tag);
        if (filter != null) {
            filter = filter.toUpperCase();
            request.setFilter(FinderRequest.Filter.valueOf(filter));
        }
        if (status != null) {
            status = status.toUpperCase();
            request.setStatus(Status.valueOf(status));
        }
        return findEnrollment.of(request);
    }

    @GetMapping("/certificate")
    public ResponseEntity<InputStreamResource> giveCertificateByCourseId(@RequestParam long courseId) throws ZerofiltreException {
        User user = securityContextManager.getAuthenticatedUser();
        Certificate certificate = null;
        try {
            certificate = certificateService.get(user, courseId);

            byte[] content = certificate.getContent();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + certificate.getPath());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(content.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(byteArrayInputStream));
        } catch (ZerofiltreException e) {
            throw new ZerofiltreException("Can't get the Certifcate from DataBase", e);
        }
    }

    @GetMapping("/certificate/verification")
    public CertificateVerificationResponseVM verifyCertificate(
            @RequestParam String uuid,
            @RequestParam String fullname,
            @RequestParam String courseTitle,
            HttpServletRequest request) throws ZerofiltreException {
        CertificateVerificationResponseVM response= new CertificateVerificationResponseVM();

        return certificateService.verify(uuid, fullname, courseTitle, request);

    }


    @PostMapping("/admin")
    public Enrollment enrollAUser(@RequestParam long courseId, @RequestParam long userId, @RequestParam(required = false) Long companyId) throws ZerofiltreException {
        return enroll.execute(userId, courseId, null == companyId ? 0 : companyId, true);
    }

    @DeleteMapping("/admin")
    public void unEnrollAUser(@RequestParam long courseId, @RequestParam long userId) throws ZerofiltreException {
        suspend.execute(userId, courseId);
    }

    @GetMapping("/admin")
    public Enrollment getEnrollmentForUser(@RequestParam long courseId, @RequestParam long userId) throws ZerofiltreException {
        return findEnrollment.of(courseId, userId, 0, true);
    }


}
