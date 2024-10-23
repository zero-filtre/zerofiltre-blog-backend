package tech.zerofiltre.blog.domain.company.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsCompanyExistsTest {

    private IsCompanyExists isCompanyExists;
    
    @Mock
    CompanyProvider companyProvider;

    @BeforeEach
    void init() {
        isCompanyExists = new IsCompanyExists(companyProvider);
    }

    @Test
    @DisplayName("given existing company when execute if company exists then return true")
    void givenExistingCompany_whenExecute_thenReturnTrue() throws ResourceNotFoundException {
        //GIVEN
        when(companyProvider.findById(anyLong())).thenReturn(Optional.of(new Company()));

        //WHEN
        boolean response = isCompanyExists.execute(1L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given non existent company when execute if company exists then throw ResourceNotFoundException")
    void givenNonExistentCompany_whenExecute_thenThrowException() {
        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> isCompanyExists.execute(1L));
    }

}