package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.storage.StorageProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.certificate.PDFCertificateEngine;
import tech.zerofiltre.blog.infra.providers.certificate.PDFCertificateProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBLessonProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
class CertificateProviderIT {

    PDFCertificateProvider pdfCertificateProvider;

    @Autowired
    SpringTemplateEngine templateEngine;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    DBChapterProvider dbChapterProvider;

    @Autowired
    DBLessonProvider dbLessonProvider;

    @Autowired
    DBEnrollmentProvider dbEnrollmentProvider;

    @Mock
    StorageProvider storageProvider;

    @Mock
    private PDFCertificateEngine certificateEngine;

    @BeforeEach
    void init() {
        pdfCertificateProvider = new PDFCertificateProvider(storageProvider, dbCourseProvider, certificateEngine);
    }

    @Test
    void generatesCertificate_Properly() throws ZerofiltreException, IOException {
        //given
        User user = new User();
        user.setFullName("Testeur Humain");

        user = dbUserProvider.save(user);

        Course course = new Course();
        course.setTitle("Cours sur les tests");
        course.setAuthor(user);

        course = dbCourseProvider.save(course);

        when(storageProvider.get(any())).thenReturn(Optional.empty());
        doNothing().when(storageProvider).store(any(), anyString());
        when(certificateEngine.process(any(), any(), anyString(), anyString())).thenReturn(new byte[]{1, 2});

        //when
        Certificate response = pdfCertificateProvider.get(user, course.getId());

        //then
        assertThat(response.getContent()).isNotEmpty();
        assertThat(response.getName()).isEqualTo("certificates/Testeur_Humain_Cours_sur_les_tests.pdf");
    }
}