package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class SavePasswordResetTest {

    public static final String TOKEN = "token";
    public static final String PASSWORD = "password";
    private SavePasswordReset savePasswordReset;

    @MockBean
    private VerificationTokenProvider verificationTokenProvider;

    @MockBean
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {
        savePasswordReset = new SavePasswordReset(verificationTokenProvider,userProvider);
    }

    @Test
    void mustCheckToken_ThenSaveUser() throws InvalidTokenException {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        //ARRANGE
        User user = new User();
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.of(new VerificationToken(user, TOKEN)));
        when(userProvider.save(any())).thenReturn(user);

        //ACT
        savePasswordReset.execute(passwordEncoder.encode(PASSWORD), TOKEN);

        //ASSERT
        verify(verificationTokenProvider,times(1)).ofToken(TOKEN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userProvider,times(1)).save(captor.capture());
        User updatedUser = captor.getValue();
        assertThat(passwordEncoder.matches(PASSWORD,updatedUser.getPassword())).isTrue();
    }
}