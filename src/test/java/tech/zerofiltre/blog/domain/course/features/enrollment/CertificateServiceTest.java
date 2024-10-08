package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CertificateServiceTest {

    private User user;

    @Mock
    private EnrollmentProvider enrollmentProvider;

    @Mock
    private CourseProvider courseProvider;

    @Mock
    CertificateProvider certificateProvider;

    @Mock
    MessageSource messageSource;


    private CertificateService certificateService;

    @BeforeEach
    void init() {
        certificateService = new CertificateService(enrollmentProvider, certificateProvider, messageSource);

        user = new User();
        user.setId(1L);
        user.setFullName("FirstName LastName");
        user.setLanguage("fr");

    }

    @Test
    void getCertificate_whenEnrollment_Is_Completed() throws ZerofiltreException {
        //given
        String courseTitle = "title course 3";
        String fileName = "name";
        byte[] content = {1, 2};

        when(enrollmentProvider.isCompleted(anyLong(), anyLong())).thenReturn(true);
        doNothing().when(enrollmentProvider).setCertificatePath(any(), anyLong(), anyLong());
        when(certificateProvider.generate(any(), anyLong())).thenReturn(new Certificate(
                fileName, courseTitle, user.getFullName(), content, "uuid", "hash"));

        //when
        Certificate response = certificateService.get(user, 2L);

        //then
        verify(enrollmentProvider, times(1)).isCompleted(anyLong(), anyLong());
        assertThat(response.getPath()).isEqualTo(fileName);
        assertThat(response.getContent()).isEqualTo(content);
    }

    @Test
    void throwZerofiltreException_whenCompletedEnrollmentIsFalse() {
        //given
        when(enrollmentProvider.isCompleted(anyLong(), anyLong())).thenReturn(false);
        when(courseProvider.getTitle(anyLong())).thenReturn("title course 3");

        //then
        Assertions.assertThatExceptionOfType(ZerofiltreException.class)
                .isThrownBy(() -> certificateService.get(user, 2L));
    }

}