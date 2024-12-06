package tech.zerofiltre.blog.domain.course.features.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsCourseExistsTest {

    private IsCourseExists isCourseExists;

    @Mock
    CourseProvider courseProvider;

    @BeforeEach
    void init() {
        isCourseExists = new IsCourseExists(courseProvider);
    }

    @Test
    @DisplayName("given an existing course when execute then return true")
    void givenExistingCompanyCourse_execute_thenNotThrowException() throws ResourceNotFoundException {
        //GIVEN
        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //WHEN
        boolean response = isCourseExists.execute(course.getId());

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given not existing course when execute then throw ResourceNotFoundException")
    void givenUnpublishedCourse_execute_thenThrowException() {
        //GIVEN
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> isCourseExists.execute(2L));
    }

}