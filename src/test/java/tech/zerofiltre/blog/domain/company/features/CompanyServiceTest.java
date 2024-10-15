package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.FoundChapterProviderSpy;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompanyServiceTest {

    private User user;
    private Course course;
    private long companyId;

    private CompanyService companyService;

    @Mock
    private CompanyProvider companyProvider;

    @Mock
    private CourseProvider courseProvider;

    @BeforeEach
    void init() {
        companyService = new CompanyService(companyProvider, courseProvider);
    }

    @Test
    void givenUserWithRoleAdmin_whenAddCourse_thenReturnCourse() throws ForbiddenActionException, UserNotFoundException {
        //GIVEN
        companyId = 1L;

        user = new User();
        user.getRoles().add("ROLE_ADMIN");

        course = new Course();

//        when(companyProvider.isUserPartOfCompany(anyLong(), anyLong())).thenReturn(true);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //WHEN
        Course response = companyService.addCourse(companyId, user, course);

        //THEN
        assertThat(response).isEqualTo(course);
        verify(companyProvider).isUserPartOfCompany(anyLong(), anyLong());
        verify(courseProvider).courseOfId(anyLong());
    }
}