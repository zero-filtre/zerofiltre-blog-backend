package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.storage.CertificatesStorageProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CertificateTest {

    private User user;

    @Mock
    private EnrollmentProvider enrollmentProvider;

    @Mock
    private CertificatesStorageProvider certificatesStorageProvider;

    @Mock
    private CourseProvider courseProvider;

    @Mock
    private ITemplateEngine templateEngine;

    private Certificate certificate;

    @BeforeEach
    void init() {
        certificate = new Certificate(enrollmentProvider, certificatesStorageProvider, courseProvider, templateEngine);

        user = new User();
        user.setId(1L);
        user.setFullName("FirstName LastName");
        user.setLanguage("fr");

    }

    @Test
    void givesCertificate_whenCompletedEnrollmentIsTrue() throws IOException, ZerofiltreException {
        //given
        String courseTitle = "title course 3";
        String fileNamePdf = ZerofiltreUtils.sanitizeString(user.getFullName()) + "_" + ZerofiltreUtils.sanitizeString(courseTitle) + ".pdf";

        when(enrollmentProvider.isCompleted(anyLong(), anyLong())).thenReturn(true);
        when(courseProvider.getTitle(anyLong())).thenReturn("title course 3");
        when(certificatesStorageProvider.get(anyString())).thenReturn(Optional.empty());
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html><head><title>Sample HTML</title></head><body><h1>Hello, OpenPDF!</h1><p>This is a paragraph in the generated PDF.</p></body></html>");

        //when
        File response = certificate.giveCertificate(user, 2L);

        //then
        assertThat(response.getName()).isEqualTo(fileNamePdf);
        verify(enrollmentProvider, times(1)).isCompleted(anyLong(), anyLong());
        verify(courseProvider, times(1)).getTitle(anyLong());
        verify(certificatesStorageProvider, times(1)).get(anyString());
        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
        verify(certificatesStorageProvider, times(1)).store(any());
    }

    @Test
    void givesCertificateFromStorage_whenCompletedEnrollmentIsTrue() throws IOException, ZerofiltreException {
        //given
        String courseTitle = "title course 3";
        String fileNamePdf = ZerofiltreUtils.sanitizeString(user.getFullName()) + "-" + ZerofiltreUtils.sanitizeString(courseTitle) + ".pdf";
        certificatesStorageProvider.store(new File(fileNamePdf));
        File pdf = new File(fileNamePdf);

        when(enrollmentProvider.isCompleted(anyLong(), anyLong())).thenReturn(true);
        when(courseProvider.getTitle(anyLong())).thenReturn(courseTitle);
        when(certificatesStorageProvider.get(anyString())).thenReturn(Optional.of(pdf));

        //when
        File response = certificate.giveCertificate(user, 2L);

        //then
        assertThat(response.getName()).isEqualTo(fileNamePdf);
        verify(enrollmentProvider, times(1)).isCompleted(anyLong(), anyLong());
        verify(courseProvider, times(1)).getTitle(anyLong());
        verify(certificatesStorageProvider, times(1)).get(anyString());
        verify(templateEngine, times(0)).process(anyString(), any(Context.class));
    }

    @Test
    void throwZerofiltreException_whenCompletedEnrollmentIsFalse() throws IOException, ZerofiltreException {
        //given
        when(enrollmentProvider.isCompleted(anyLong(), anyLong())).thenReturn(false);
        when(courseProvider.getTitle(anyLong())).thenReturn("title course 3");
        when(certificatesStorageProvider.get(anyString())).thenReturn(Optional.empty());

        //then
        Assertions.assertThatExceptionOfType(ZerofiltreException.class)
                .isThrownBy(() -> certificate.giveCertificate(user, 2L))
                .withMessage("The certificate cannot be issued. The course has not yet been completed.");
    }

    @Test
    void throwZerofiltreException_whenTemplateEngineProcessReturnNull() throws IOException, ZerofiltreException {
        //given
        when(enrollmentProvider.isCompleted(anyLong(), anyLong())).thenReturn(true);
        when(courseProvider.getTitle(anyLong())).thenReturn("title course 3");
        when(certificatesStorageProvider.get(anyString())).thenReturn(Optional.empty());
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(null);

        //then
        Assertions.assertThatExceptionOfType(ZerofiltreException.class)
                .isThrownBy(() -> certificate.giveCertificate(user, 2L))
                .withMessage("Error during certificate generation.");
    }

}