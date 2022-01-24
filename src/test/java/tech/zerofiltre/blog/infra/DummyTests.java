package tech.zerofiltre.blog.infra;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.config.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@ExtendWith(SpringExtension.class)
@Import({WebConfiguration.class,PasswordEncoderConfiguration.class})
class DummyTests {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void mustMatch() {
        String encodedPassword = "$2a$10$HvU/EGupeW/5g4QKPTR9x.f3o2wJao1.5eCiXttgR10gS4pOsKP86";
        String lowerCaseEncodedPassword = encodedPassword.toLowerCase();
        assertThat(passwordEncoder
                .matches("string", encodedPassword))
                .isTrue();

        assertThat(passwordEncoder
                .matches("string", lowerCaseEncodedPassword))
                .isFalse();

        assertThat(passwordEncoder
                .matches("string", "$2a$10$GMDsdYdn2WHLYcXzbqx5qOUuFZXV8LqMsNPAV4pRxViTzaTfTTzMm"))
                .isTrue();
    }

}
