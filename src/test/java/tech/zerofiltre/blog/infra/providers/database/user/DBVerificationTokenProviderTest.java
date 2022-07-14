package tech.zerofiltre.blog.infra.providers.database.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class DBVerificationTokenProviderTest {

    public static final String TOKEN = "TOKEN";
    DBVerificationTokenProvider provider;

    @MockBean
    VerificationTokenJPARepository repository;

    User user = new User();

    @BeforeEach
    void init() {
        provider = new DBVerificationTokenProvider(repository);
    }

    @Test
    void generateToken_mustSaveNewToken() {
        //ARRANGE
        LocalDateTime beforeSavePlus1d = LocalDateTime.now().plusDays(1);


        //ACT
        provider.generate(user);

        //ASSERT
        LocalDateTime afterSavePlus1d = LocalDateTime.now().plusDays(1);
        ArgumentCaptor<VerificationTokenJPA> captor = ArgumentCaptor.forClass(VerificationTokenJPA.class);
        verify(repository, times(1)).save(captor.capture());
        VerificationTokenJPA verificationToken = captor.getValue();
        assertThat(verificationToken).isNotNull();
        assertThat(verificationToken.getExpiryDate()).isBeforeOrEqualTo(afterSavePlus1d);
        assertThat(verificationToken.getExpiryDate()).isAfterOrEqualTo(beforeSavePlus1d);
    }

    @Test
    void updateToken_mustCheckTokenAndExtendExpiryDate_ThenSave_ifTokenExists() {
        //ARRANGE
        VerificationTokenJPA verificationToken = new VerificationTokenJPA();
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(24));
        when(repository.findByUser(any())).thenReturn(Optional.of(verificationToken));
        LocalDateTime previousExpiryDate = verificationToken.getExpiryDate();

        //ACT
        provider.generate(user);

        //ASSERT
        verify(repository, times(1)).findByUser(any());
        ArgumentCaptor<VerificationTokenJPA> captor = ArgumentCaptor.forClass(VerificationTokenJPA.class);
        verify(repository, times(1)).save(captor.capture());
        LocalDateTime afterSavePlus1d = LocalDateTime.now().plusDays(1);

        VerificationTokenJPA savedVerificationToken = captor.getValue();
        assertThat(savedVerificationToken).isNotNull();

        LocalDateTime expiryDate = savedVerificationToken.getExpiryDate();
        assertThat(expiryDate).isBeforeOrEqualTo(afterSavePlus1d);
        assertThat(expiryDate).isAfterOrEqualTo(previousExpiryDate);


    }

    @Test
    void updateToken_mustCheckTokenAndCreateToken_ThenSave_ifTokenDoesNotExist() {
        //ARRANGE
        when(repository.findByUser(any())).thenReturn(Optional.empty());
        LocalDateTime beforeSavePlus1d = LocalDateTime.now().plusDays(1);


        //ACT
        provider.generate(user);

        //ASSERT
        verify(repository, times(1)).findByUser(any());

        ArgumentCaptor<VerificationTokenJPA> captor = ArgumentCaptor.forClass(VerificationTokenJPA.class);
        verify(repository, times(1)).save(captor.capture());
        LocalDateTime afterSavePlus1d = LocalDateTime.now().plusDays(1);

        VerificationTokenJPA savedVerificationToken = captor.getValue();
        assertThat(savedVerificationToken).isNotNull();

        LocalDateTime expiryDate = savedVerificationToken.getExpiryDate();
        assertThat(expiryDate).isBeforeOrEqualTo(afterSavePlus1d);
        assertThat(expiryDate).isAfterOrEqualTo(beforeSavePlus1d);
    }
}