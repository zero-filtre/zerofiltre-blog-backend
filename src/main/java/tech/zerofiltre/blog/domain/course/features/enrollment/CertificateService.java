package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.CertificateVerificationFailedException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;

import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.CertificateVerificationResponseVM;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.AssertTrue;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateService {

    private final EnrollmentProvider enrollmentProvider;
    private final CertificateProvider certificateProvider;

    private final MessageSource messageSource;


    public Certificate get(User user, long courseId) throws ZerofiltreException {
        if (enrollmentProvider.isCompleted(user.getId(), courseId)) {
            Certificate certificate = certificateProvider.generate(user, courseId);
            enrollmentProvider.setCertificatePath(certificate.getPath(), user.getId(), courseId);

            certificateProvider.save(certificate);

            return certificate;
        }
        throw new ZerofiltreException("The certificate cannot be issued. The course has not yet been completed.");
    }


    public CertificateVerificationResponseVM verify(String uuid, String fullname, String courseTitle, HttpServletRequest request) throws ZerofiltreException {

        CertificateVerificationResponseVM response = new CertificateVerificationResponseVM();
        try {
            Certificate dbCertificate = certificateProvider.findByUuid(uuid);
            if(dbCertificate == null){
                // Cas où le certificat n'est pas trouvé (QRCode non valide, UUID erroné, etc.)
                response.setResponse(messageSource.getMessage("message.certificate.verification.response.error", new Object[]{}, request.getLocale()));
                response.setDescription(messageSource.getMessage("message.certificate.verification.description.error", new Object[]{}, request.getLocale()));
            }
            String collectedHash = ZerofiltreUtils.generateHash(fullname, courseTitle);
            String dbHash = dbCertificate.getHash();

            if (collectedHash.equals(dbHash)) {
                response.setOwnerFullName(dbCertificate.getOwnerFullName());
                response.setCourseTitle(dbCertificate.getCourseTitle());
                response.setResponse(messageSource.getMessage("message.certificate.verification.response.valid", new Object[]{}, request.getLocale()));
                response.setDescription(messageSource.getMessage("message.certificate.verification.description.valid", new Object[]{}, request.getLocale()));
            } else {
                response.setResponse(messageSource.getMessage("message.certificate.verification.response.invalid", new Object[]{}, request.getLocale()));
                response.setDescription(messageSource.getMessage("message.certificate.verification.description.invalid", new Object[]{}, request.getLocale()));
            }

        } catch (NoSuchAlgorithmException e) {
            throw new ZerofiltreException("Hash generation failure !", e);
        }catch (Exception e){
            // Capture toutes les autres erreurs imprévues
            response.setResponse(messageSource.getMessage("message.certificate.verification.response.error", new Object[]{}, request.getLocale()));
            response.setDescription(messageSource.getMessage("message.certificate.verification.description.error", new Object[]{}, request.getLocale()));
        }
        return response;

    }


}
