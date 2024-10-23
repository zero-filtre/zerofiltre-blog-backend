package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import tech.zerofiltre.blog.domain.company.features.CompanyUserService;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyUserControllerTest {

    CompanyUserController controller;

    @Mock
    SecurityContextManager securityContextManager;

    @Mock
    CompanyUserService companyUserService;

    @Mock
    MessageSource sources;

    @BeforeEach
    void setUp() {
        controller = new CompanyUserController(securityContextManager, companyUserService, sources);
    }

    @Test
    void givenCompanyIdAndDelete_whenUnLinkAll_thenVerifyCallCompanyUserService_unlinkAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyUserService).unlinkAllByCompanyId(any(User.class), anyLong());

        //ACT
        controller.unLinkAll(1L, null);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService).unlinkAllByCompanyId(any(User.class), anyLong());
    }

    @Test
    void givenCompanyIdAndUserIdAndDelete_whenUnLinkAll_thenVerifyCallCompanyUserService_unlinkAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyUserService).unlinkAllByCompanyId(any(User.class), anyLong());

        //ACT
        controller.unLinkAll(1L, 1L);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService).unlinkAllByCompanyId(any(User.class), anyLong());
    }

    @Test
    void givenUserIdAndDelete_whenUnLinkAll_thenVerifyCallCompanyUserService_unLinkAllByUserId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyUserService).unlinkAllByUserId(any(User.class), anyLong());

        //ACT
        controller.unLinkAll(null, 1L);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService).unlinkAllByUserId(any(User.class), anyLong());
    }

    @Test
    void givenDelete_whenUnLinkAll_thenVerifyNotCallCompanyUserService() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());

        //ACT
        controller.unLinkAll(null, null);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService, never()).unlinkAllByCompanyId(any(User.class), anyLong());
        verify(companyUserService, never()).unlinkAllByUserId(any(User.class), anyLong());
    }

}