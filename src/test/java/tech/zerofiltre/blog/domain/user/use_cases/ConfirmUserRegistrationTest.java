package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ConfirmUserRegistrationTest {


    public static final String TOKEN = "token";
    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    @MockBean
    UserProvider userProvider;

    ConfirmUserRegistration confirmUserRegistration;

    User user = new User();
    VerificationToken verificationToken = new VerificationToken(user, TOKEN);


    @BeforeEach
    void setUp() {
        confirmUserRegistration = new ConfirmUserRegistration(verificationTokenProvider, userProvider);
    }

    @Test
    void onValidToken_setUserActive_thenSave() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.ofNullable(verificationToken));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        assertThat(user.isActive()).isFalse();

        //ACT
        user = confirmUserRegistration.execute(TOKEN);

        //ASSERT
        assertThat(user.isActive()).isTrue();
        verify(userProvider, times(1)).save(any());

    }

    @Test
    void onTokenExpired_ThrowInvalidTokenException_withProperMessage() {
        //ARRANGE
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.ofNullable(verificationToken));

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidTokenException.class).isThrownBy(() -> confirmUserRegistration.execute(TOKEN))
                .withMessage(ConfirmUserRegistration.TOKEN_EXPIRED);

    }

    @Test
    void onTokenNotFound_ThrowInvalidTokenException_withProperMessage() {
        //ARRANGE
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidTokenException.class).isThrownBy(() -> confirmUserRegistration.execute(TOKEN))
                .withMessage(ConfirmUserRegistration.INVALID_TOKEN);

    }
}