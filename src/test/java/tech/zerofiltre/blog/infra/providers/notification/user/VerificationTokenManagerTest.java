package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class VerificationTokenManagerTest {

    public static final String TOKEN = "token";
    private VerificationTokenManager verificationTokenManager;

    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    User user = new User();

    @BeforeEach
    void setUp() {
        verificationTokenManager = new VerificationTokenManager(verificationTokenProvider);
        when(verificationTokenProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

    }

    @Test
    void generateToken_mustSaveNewToken() {
        //ARRANGE
        LocalDateTime beforeSavePlus1d = LocalDateTime.now().plusDays(1);


        //ACT
        verificationTokenManager.generateToken(user);

        //ASSERT
        LocalDateTime afterSavePlus1d = LocalDateTime.now().plusDays(1);
        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenProvider, times(1)).save(captor.capture());
        VerificationToken verificationToken = captor.getValue();
        assertThat(verificationToken).isNotNull();
        assertThat(verificationToken.getExpiryDate()).isBeforeOrEqualTo(afterSavePlus1d);
        assertThat(verificationToken.getExpiryDate()).isAfterOrEqualTo(beforeSavePlus1d);
    }

    @Test
    void updateToken_mustCheckTokenAndExtendExpiryDate_ThenSave_ifTokenExists() {
        //ARRANGE
        VerificationToken verificationToken = new VerificationToken(user, TOKEN);
        when(verificationTokenProvider.ofUser(user)).thenReturn(Optional.of(verificationToken));
        LocalDateTime previousExpiryDate = verificationToken.getExpiryDate();

        //ACT
        verificationTokenManager.generateToken(user);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofUser(user);
        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenProvider, times(1)).save(captor.capture());
        LocalDateTime afterSavePlus1d = LocalDateTime.now().plusDays(1);

        VerificationToken savedVerificationToken = captor.getValue();
        assertThat(savedVerificationToken).isNotNull();

        LocalDateTime expiryDate = savedVerificationToken.getExpiryDate();
        assertThat(expiryDate).isBeforeOrEqualTo(afterSavePlus1d);
        assertThat(expiryDate).isAfterOrEqualTo(previousExpiryDate);


    }

    @Test
    void updateToken_mustCheckTokenAndCreateToken_ThenSave_ifTokenDoesNotExist() {
        //ARRANGE
        when(verificationTokenProvider.ofUser(user)).thenReturn(Optional.empty());
        LocalDateTime beforeSavePlus1d = LocalDateTime.now().plusDays(1);


        //ACT
        verificationTokenManager.generateToken(user);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofUser(user);

        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenProvider, times(1)).save(captor.capture());
        LocalDateTime afterSavePlus1d = LocalDateTime.now().plusDays(1);

        VerificationToken savedVerificationToken = captor.getValue();
        assertThat(savedVerificationToken).isNotNull();

        LocalDateTime expiryDate = savedVerificationToken.getExpiryDate();
        assertThat(expiryDate).isBeforeOrEqualTo(afterSavePlus1d);
        assertThat(expiryDate).isAfterOrEqualTo(beforeSavePlus1d);
    }
}