package tech.zerofiltre.blog.infra.providers.database.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(DBVerificationTokenProvider.class)
@TestPropertySource(properties = {"zerofiltre.infra.security.verification-token.expiration-seconds=604800"})
class DBVerificationTokenProviderIT {

    @Autowired
    DBVerificationTokenProvider provider;

    @MockBean
    VerificationTokenJPARepository repository;

    @BeforeEach
    void setUp() {
        when(repository.findByUser(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    @Test
    void shouldGenerate_withConfigured_value() {
        //given
        User user = ZerofiltreUtilsTest.createMockUser(false);
        LocalDateTime beforeSavePlus7d = LocalDateTime.now().plusDays(7).minusSeconds(30);

        //when
        VerificationToken verificationToken = provider.generate(user);
        LocalDateTime afterSavePlus7d = LocalDateTime.now().plusDays(7).plusSeconds(30);

        //then
        assertThat(verificationToken.getExpiryDate()).isStrictlyBetween(beforeSavePlus7d, afterSavePlus7d);
    }
}