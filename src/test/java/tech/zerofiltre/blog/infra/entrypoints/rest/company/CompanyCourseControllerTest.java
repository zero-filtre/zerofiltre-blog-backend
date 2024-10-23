package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import tech.zerofiltre.blog.domain.company.features.CompanyCourseService;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

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
    MessageSource sources;

    @BeforeEach
    void setUp() {
        controller = new CompanyCourseController(securityContextManager, companyCourseService, sources);
    }

    @Test
    void givenCompanyIdAndDelete_whenUnlinkAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyCourseService).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());

        //ACT
        controller.unlinkAll(1L, null, true);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());
    }

    @Test
    void givenCompanyIdAndCourseIdAndDelete_whenUnlinkAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyCourseService).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());

        //ACT
        controller.unlinkAll(1L, 1L, true);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());
    }

    @Test
    void givenCourseIdAndDelete_whenUnLinkAll_thenVerifyCallCompanyCourseService_unlinkAllByCourseId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyCourseService).unlinkAllByCourseId(any(User.class), anyLong(), anyBoolean());

        //ACT
        controller.unlinkAll(null, 1L, true);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService).unlinkAllByCourseId(any(User.class), anyLong(), anyBoolean());
    }

    @Test
    void givenDelete_whenUnlinkAll_thenVerifyNotCallCompanyCourseService() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());

        //ACT
        controller.unlinkAll(null, null, true);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyCourseService, never()).unlinkAllByCompanyId(any(User.class), anyLong(), anyBoolean());
        verify(companyCourseService, never()).unlinkAllByCourseId(any(User.class), anyLong(), anyBoolean());
    }

}