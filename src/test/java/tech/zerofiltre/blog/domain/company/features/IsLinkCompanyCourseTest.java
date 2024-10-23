package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsLinkCompanyCourseTest {

    private IsCompanyCourse isCompanyCourse;

    @Mock
    CompanyCourseProvider companyCourseProvider;

    @BeforeEach
    void init() {
        isCompanyCourse = new IsCompanyCourse(companyCourseProvider);
    }

    @Test
    @DisplayName("given existing company when execute if company exists then return true")
    void givenExistingCompany_whenExecute_thenReturnTrue() throws ResourceNotFoundException {
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyCourse()));

        //WHEN
        boolean response = isCompanyCourse.execute(1, 1);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given non existent company course when execute then throw ResourceNotFoundException")
    void givenNotExistingCompanyCourse_whenExecute_thenThrowException() {
        //GIVEN
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> isCompanyCourse.execute(1, 1));
    }

    @Test
    @DisplayName("given existing company when getCompanyCourseIdIfCourseIsActive then return companyCourseId")
    void givenExistingCompany_whenGetCompanyCourseId_thenReturnCompanyCourseIdIfCourseIsActive() throws ResourceNotFoundException {
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(1, 2L, 3L, true, LocalDateTime.now(), null);
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.of(linkCompanyCourse));

        //WHEN
        long response = isCompanyCourse.getCompanyCourseIdIfCourseIsActive(1, 1);

        //THEN
        assertThat(response).isEqualTo(linkCompanyCourse.getId());
    }

    @Test
    @DisplayName("given non existent company course when getCompanyCourseIdIfCourseIsActive then throw ResourceNotFoundException")
    void givenNotExistingCompanyCourse_whenGetCompanyCourseId_IfCourseIsActive_thenThrowException() {
        //GIVEN
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.empty());

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> isCompanyCourse.getCompanyCourseIdIfCourseIsActive(1, 1));
    }

    @Test
    @DisplayName("given an not active company course when getCompanyCourseIdIfCourseIsActive then throw ResourceNotFoundException")
    void givenNotActiveCompanyCourse_whenGetCompanyCourseId_IfCourseIsActive_thenThrowException() {
        //GIVEN
        when(companyCourseProvider.linkOf(anyLong(), anyLong())).thenReturn(Optional.of(new LinkCompanyCourse(1, 1, 1, false, LocalDateTime.now(), LocalDateTime.now())));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> isCompanyCourse.getCompanyCourseIdIfCourseIsActive(1, 1));
    }

}