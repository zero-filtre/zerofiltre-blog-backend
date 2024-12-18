package tech.zerofiltre.blog.infra.entrypoints.rest.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.features.CompanyUserService;
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
class CompanyUserControllerTest {

    CompanyUserController controller;

    @Mock
    SecurityContextManager securityContextManager;

    @Mock
    MessageSource sources;

    @Mock
    CompanyUserProvider companyUserProvider;

    @Mock
    DataChecker checker;

    @Mock
    CompanyUserService companyUserService;

    @BeforeEach
    void setUp() {
        controller = new CompanyUserController(securityContextManager, sources, companyUserProvider, checker);
        ReflectionTestUtils.setField(controller, "companyUserService", companyUserService);
    }

    @Test
    @DisplayName("given companyId when unlinkAllByCompanyId then verify call companyUserService unlinkAllByCompanyId")
    void givenCompanyId_whenUnlinkAllByCompanyId_thenVerifyCallCompanyUserServiceUnlinkAllByCompanyId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyUserService).unlinkAllByCompanyId(any(User.class), anyLong());

        //ACT
        controller.unlinkAllByCompanyId(1L);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService).unlinkAllByCompanyId(any(User.class), anyLong());
    }

    @Test
    @DisplayName("given bad companyId when unlinkAllByCompanyId then throw ForbiddenActionException")
    void givenBadCompanyId_whenUnlinkAllByCompanyId_thenThrowException() throws ResourceNotFoundException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());

        //ACT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> controller.unlinkAllByCompanyId(-1L));

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService, never()).unlinkAllByCompanyId(any(User.class), anyLong());
    }

    @Test
    @DisplayName("given userId when unlinkAllByUserId then verify call companyUserService unlinkAllByUserId")
    void givenUserId_whenUnlinkAllByUserId_thenVerifyCallCompanyUserServiceUnlinkAllByUserId() throws ResourceNotFoundException, ForbiddenActionException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());
        doNothing().when(companyUserService).unlinkAllByUserId(any(User.class), anyLong());

        //ACT
        controller.unlinkAllByUserId(1L);

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService).unlinkAllByUserId(any(User.class), anyLong());
    }

    @Test
    @DisplayName("given bad userId when unlinkAllByUserId then throw ForbiddenActionException")
    void givenBadUserId_whenUnlinkAllByUserId_thenThrowException() throws ResourceNotFoundException {
        //ARRANGE
        when(securityContextManager.getAuthenticatedUser()).thenReturn(new User());

        //ACT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> controller.unlinkAllByCompanyId(-1L));

        //ASSERT
        verify(securityContextManager).getAuthenticatedUser();
        verify(companyUserService, never()).unlinkAllByCompanyId(any(User.class), anyLong());
    }

}