package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.util.DataChecker;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyCourseControllerTest {

    CompanyCourseController controller;

    @Mock
    SecurityContextManager securityContextManager;

    @Mock
    CompanyCourseService companyCourseService;

    @Mock
    CompanyCourseProvider companyCourseProvider;

    @Mock
    DataChecker checker;

    @BeforeEach
    void setUp() {
        controller = new CompanyCourseController(securityContextManager, companyCourseProvider, checker);
        ReflectionTestUtils.setField(controller, "companyCourseService", companyCourseService);
    }

    @Test
    @DisplayName("given companyId and hard when unlinkAllByCompanyId then verify call companyCourseService unlinkAllByCompanyId")
    void givenCompanyIdAndHard_whenUnlinkAllByCompanyId_thenVerifyCallCompanyCourseServiceUnlinkAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyCourseService).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());

        //ACT
        controller.unlinkAllByCompanyId(1L, true);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("given bad companyId when unlinkAllByCompanyId then throw ForbiddenActionException")
    void givenBadCompanyId_whenUnlinkAllByCompanyId_thenThrowException() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());

        //ACT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> controller.unlinkAllByCompanyId(-1L, true));

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService, never()).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("given courseId and hard when unlinkAllByCourseId then verify call companyCourseService unlinkAllByCourseId")
    void givenCourseIdAndHard_whenUnlinkAllByCourseId_thenVerifyCallCompanyCourseServiceUnlinkAllByCourseId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyCourseService).unlinkAllByCourseId(any(User.class), anyLong(), anyBoolean());

        //ACT
        controller.unlinkAllByCourseId(1L, true);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService).unlinkAllByCourseId(any(User.class), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("given bad courseId when unlinkAllByCourseId then throw ForbiddenActionException")
    void givenBadCourseId_whenUnlinkAllByCourseId_thenThrowException() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());

        //ACT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> controller.unlinkAllByCompanyId(-1L, true));

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService, never()).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());
    }

}