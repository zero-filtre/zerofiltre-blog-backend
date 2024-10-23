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
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsPublishedCourseTest {

    private IsPublishedCourse isPublishedCourse;

    @Mock
    CourseProvider courseProvider;

    @BeforeEach
    void init() {
        isPublishedCourse = new IsPublishedCourse(courseProvider);
    }

    @Test
    @DisplayName("given an existing published course when execute then not throw exception")
    void givenExistingPublishedCourse_execute_thenNotThrowException() throws ForbiddenActionException {
        //GIVEN
        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, new User(), Collections.emptyList(), Collections.emptyList());

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //WHEN
        boolean response = isPublishedCourse.execute(course.getId());

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given not existing course when execute then not throw exception")
    void givenNotExistingCourse_execute_thenNotThrowException() {
        //GIVEN
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.empty());

        //THEN
        assertDoesNotThrow(() -> isPublishedCourse.execute(1L));
    }

    @Test
    @DisplayName("given draft course when execute then throw ForbiddenActionException")
    void givenUnpublishedCourse_execute_thenThrowException() {
        //GIVEN
        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());

        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.of(course));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> isPublishedCourse.execute(2L));
    }

}