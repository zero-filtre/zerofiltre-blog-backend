package tech.zerofiltre.blog.infra.providers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.json.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.security.model.*;

import java.time.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@JsonTest
@Import(JwtAuthenticationTokenProperties.class)
class SimpleJwtTokenProviderIT {

    @Autowired
    JwtAuthenticationTokenProperties jwtAuthenticationTokenProperties;

    SimpleJwtTokenProvider provider;
    User user = new User();

    @BeforeEach
    void setUp() {
        provider = new SimpleJwtTokenProvider(jwtAuthenticationTokenProperties);
    }

    @Test
    void generate() {
        //ARRANGE
        LocalDateTime beforeGeneration = LocalDateTime.now();

        //ACT
        JwtToken result = provider.generate(user);

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isNotNull();
        assertThat(result.getAccessToken()).isNotEmpty();
        long expiryInSeconds = result.getExpiryDateInSeconds();
        assertThat(expiryInSeconds).isNotZero();
        LocalDateTime afterGeneration = LocalDateTime.now().plusDays(1).plusMinutes(1);
        assertThat(LocalDateTime.ofEpochSecond(expiryInSeconds, 0, ZoneOffset.UTC)).isAfterOrEqualTo(beforeGeneration);
        assertThat(LocalDateTime.ofEpochSecond(expiryInSeconds, 0, ZoneOffset.UTC)).isBeforeOrEqualTo(afterGeneration);


    }
}