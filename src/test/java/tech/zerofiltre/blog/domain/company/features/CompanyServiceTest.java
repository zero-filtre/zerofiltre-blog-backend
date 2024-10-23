package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    private CompanyService companyService;

    @Mock
    private CompanyProvider companyProvider;

    @Mock
    private CompanyUserProvider companyUserProvider;

    @Mock
    private CompanyCourseProvider companyCourseProvider;

    @Mock
    DataChecker checker;

    @BeforeEach
    void init() {
        companyService = new CompanyService(companyProvider, companyUserProvider, companyCourseProvider, checker);
    }

    @Test
    @DisplayName("given admin user and new company when save then verify call companyProvider save")
    void whenSave_thenVerifyCallCompanyProviderSave() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);

        //WHEN
        companyService.save(new User(), new Company());

        //THEN
        verify(companyProvider).save(any(Company.class));
    }

    @Test
    @DisplayName("given bad user when save then throw ForbiddenActionException")
    void givenUserWithRoleUserAndNewCompany_whenSave_thenThrowForbiddenActionException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyService.save(new User(), new Company()));
    }

    @Test
    @DisplayName("given company admin user and existent company when patch then verify call companyProvider save")
    void whenPatch_thenVerifyCallCompanyProviderSave() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyService.patch(new User(), new Company());

        //THEN
        verify(companyProvider).save(any(Company.class));
    }

    @Test
    @DisplayName("given bad user when patch then throw ForbiddenActionException")
    void givenBadUser_whenPatch_thenThrowForbiddenActionException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyService.patch(new User(), new Company()));
    }

    @Test
    @DisplayName("given admin user and not exiting company when patch then throw ResourceNotFoundException")
    void givenNotExitingCompany_whenPatch_thenThrowResourceNotFoundException() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);
        when(checker.companyExists(anyLong())).thenThrow(new ResourceNotFoundException("", ""));

        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> companyService.patch(new User(), new Company()));
    }

    @Test
    @DisplayName("given admin user and existing company id when findById then return company")
    void whenFindById_thenReturnCompany() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenReturn(true);

        //WHEN
        companyService.findById(new User(), 1L);

        //THEN
        verify(companyProvider).findById(anyLong());
    }

    @Test
    @DisplayName("given bad user when findById then throw ForbiddenActionException")
    void givenBadUser_whenFindById_thenThrowForbiddenActionException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminOrCompanyAdmin(any(User.class), anyLong())).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyService.findById(new User(), 1L));
    }

    @Test
    @DisplayName("given admin user when findAll then verify call companyProvider findAll")
    void _whenFindALl_thenVerifyCallCompanyProviderFindAll() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);

        //WHEN
        companyService.findAll(new User(), 0, 10);

        //THEN
        verify(companyProvider).findAll(0, 10);
    }

    @Test
    @DisplayName("given admin user when findAll then throw ForbiddenActionException")
    void givenBadUser_whenFindAll_thenThrowForbiddenActionException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyService.findAll(new User(), 0,10));
    }

    @Test
    @DisplayName("given admin user and existing company when delete then verify call companyUserProvider unlinkAllByCompanyId and companyCourseProvider unlinkAllByCompanyId and companyProvider delete")
    void givenAdminUserAndExistingCompany_whenDelete_thenVerifyCallCompanyProviderDelete() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenReturn(true);
        when(checker.companyExists(anyLong())).thenReturn(true);

        //WHEN
        companyService.delete(new User(), new Company());

        //THEN
        verify(companyUserProvider).deleteAllByCompanyId(anyLong());
        verify(companyCourseProvider).deleteAllByCompanyId(anyLong());
        verify(companyProvider).delete(any(Company.class));
    }

    @Test
    @DisplayName("given admin user when findAll then throw ForbiddenActionException")
    void givenBadUser_whenDelete_thenThrowForbiddenActionException() throws ForbiddenActionException {
        //GIVEN
        when(checker.isAdminUser(any(User.class))).thenThrow(new ForbiddenActionException(""));

        //THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> companyService.delete(new User(), new Company()));

        verify(companyUserProvider, never()).deleteAllByCompanyId(anyLong());
        verify(companyCourseProvider, never()).deleteAllByCompanyId(anyLong());
        verify(companyProvider, never()).delete(any(Company.class));
    }
}
