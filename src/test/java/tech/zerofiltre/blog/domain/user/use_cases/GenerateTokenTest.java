package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.security.model.*;

import java.time.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class GenerateTokenTest {

    public static final String TOKEN = "token";
    private GenerateToken generateToken;

    @MockBean
    VerificationTokenProvider verificationTokenProvider;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {

        generateToken = new GenerateToken(verificationTokenProvider, jwtTokenProvider, userProvider);
    }

    @Test
    void execute_generates_ProperlyFromRefreshToken() throws InvalidTokenException {
        //ARRANGE
        User user = new User();
        VerificationToken verificationToken = new VerificationToken(user, TOKEN);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.of(verificationToken));
        when(jwtTokenProvider.generate(user))
                .thenReturn(new JwtToken("accessToken", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

        //ACT
        Token token = generateToken.byRefreshToken(TOKEN);

        assertThat(token).isNotNull();

        assertThat(token.getRefreshToken()).isNotNull();
        assertThat(token.getAccessToken()).isNotNull();
        assertThat(token.getTokenType()).isNotNull();
        assertThat(token.getTokenType()).isNotEmpty();
        assertThat(token.getRefreshTokenExpiryDateInSeconds()).isNotZero();
        assertThat(token.getAccessTokenExpiryDateInSeconds()).isNotZero();
    }


    @Test
    void execute_generates_ProperlyFromUser() {
        //ARRANGE
        User user = new User();
        VerificationToken verificationToken = new VerificationToken(user, TOKEN);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        when(verificationTokenProvider.generate(eq(user), anyLong())).thenReturn(verificationToken);
        when(jwtTokenProvider.generate(user))
                .thenReturn(new JwtToken("accessToken", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

        //ACT
        Token token = generateToken.byUser(user);

        assertThat(token).isNotNull();

        assertThat(token.getRefreshToken()).isNotNull();
        assertThat(token.getAccessToken()).isNotNull();
        assertThat(token.getTokenType()).isNotNull();
        assertThat(token.getTokenType()).isNotEmpty();
        assertThat(token.getRefreshTokenExpiryDateInSeconds()).isNotZero();
        assertThat(token.getAccessTokenExpiryDateInSeconds()).isNotZero();
    }
}