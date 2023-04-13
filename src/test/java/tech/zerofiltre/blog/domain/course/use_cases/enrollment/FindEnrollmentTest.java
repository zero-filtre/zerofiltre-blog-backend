package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.domain.FinderRequest.Filter.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;


class FindEnrollmentTest {

    @Test
    void findEnrollment_returns_theProperPage() {
        //given
        EnrollmentProviderSpy enrollmentProviderSpy = new EnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindEnrollment findEnrollment = new FindEnrollment(enrollmentProviderSpy, courseProvider, chapterProvider);
        //when
        FinderRequest request = new FinderRequest(0, 3, DRAFT, new User());
        Page<Course> courses = findEnrollment.of(request);

        //then
        assertThat(enrollmentProviderSpy.ofCalled).isTrue();
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(2);
        assertThat(courses.getHasNext()).isTrue();
        assertThat(courses.getHasPrevious()).isTrue();
        assertThat(courses.getNumberOfElements()).isEqualTo(2);
        assertThat(courses.getPageNumber()).isEqualTo(1);
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(10);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(4);
        courses.getContent().forEach(course -> {
            assertThat(course.getEnrolledCount()).isEqualTo(1);
            assertThat(course.getLessonsCount()).isEqualTo(2);
        });
    }

    @Test
    void findEnrollment_calls_EnrollmentProvider_withTheInactiveParam() {
        //given
        EnrollmentProviderSpy enrollmentProvider = new EnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindEnrollment findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
        //when
        FinderRequest request = new FinderRequest(0, 3, DRAFT, new User());
        request.setFilter(INACTIVE);
        findEnrollment.of(request);

        //then
        assertThat(enrollmentProvider.ofCalled).isTrue();
        assertThat(enrollmentProvider.ofFilter).isNotNull();
        assertThat(enrollmentProvider.ofFilter).isEqualTo(INACTIVE);
    }

    @Test
    void findEnrollment_calls_EnrollmentProvider_withTheCompletedParam() {
        //given
        EnrollmentProviderSpy enrollmentProvider = new EnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindEnrollment findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
        //when
        FinderRequest request = new FinderRequest(0, 3, DRAFT, new User());
        request.setFilter(COMPLETED);
        findEnrollment.of(request);

        //then
        assertThat(enrollmentProvider.ofCalled).isTrue();
        assertThat(enrollmentProvider.ofFilter).isNotNull();
        assertThat(enrollmentProvider.ofFilter).isEqualTo(COMPLETED);
    }

    @Test
    void findAEnrollment_returns_theProperOne() throws ResourceNotFoundException, ForbiddenActionException {
        //given
        EnrollmentProviderSpy enrollmentProvider = new EnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindEnrollment findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
        User executor = ZerofiltreUtils.createMockUser(true);
        //when
        Enrollment enrollment = findEnrollment.of(0, 1, executor);
        //then
        assertThat(enrollment).isNotNull();

    }

    @Test
    void findAnEnrollment_throwsResourceNotFoundException() {
        //given
        EnrollmentProvider enrollmentProvider = new NotFoundEnrollmentProviderDummy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindEnrollment findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
        User executor = ZerofiltreUtils.createMockUser(true);

        //when
        //then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> findEnrollment.of(1, 1, executor))
                .withMessage("Enrollment not found");
    }

    @Test
    void findAnEnrollment_throwsForbiddenActionException_ifExecutor_isNotAdmin_NorInvolved() {
        //given
        EnrollmentProvider enrollmentProvider = new EnrollmentProviderSpy();
        CourseProvider courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy();
        ChapterProvider chapterProvider = new FoundChapterProviderSpy();
        FindEnrollment findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);
        User executor = ZerofiltreUtils.createMockUser(false);

        //when
        //then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> findEnrollment.of(1, 1, executor))
                .withMessage("You are only allow to look for your enrollments");
    }

}