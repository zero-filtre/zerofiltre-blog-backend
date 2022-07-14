package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBUserProvider.class, DBVerificationTokenProvider.class})
class ConfirmUserRegistrationIT {


    public static final String TOKEN = "token";

    @Autowired
    VerificationTokenProvider verificationTokenProvider;

    @Autowired
    UserProvider userProvider;


    ConfirmUserRegistration confirmUserRegistration;

    User user = new User();
    VerificationToken verificationToken;


    @BeforeEach
    void setUp() {
        user = userProvider.save(user);
        verificationToken = new VerificationToken(user, TOKEN);
        verificationTokenProvider.save(verificationToken);
        confirmUserRegistration = new ConfirmUserRegistration(verificationTokenProvider, userProvider);
    }

    @Test
    void onValidToken_setUserActive_thenSave() throws InvalidTokenException {
        //ARRANGE
        assertThat(user.isActive()).isFalse();

        //ACT
        user = confirmUserRegistration.execute(TOKEN);

        //ASSERT
        assertThat(user.isActive()).isTrue();

    }

}