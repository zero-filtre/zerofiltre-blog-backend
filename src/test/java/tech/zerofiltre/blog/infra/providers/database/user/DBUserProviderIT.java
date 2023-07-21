package tech.zerofiltre.blog.infra.providers.database.user;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;
import tech.zerofiltre.blog.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
class DBUserProviderIT {

    public static final String TOKEN = "token";
    DBUserProvider provider;

    @Autowired
    UserJPARepository userJPARepository;
    @Autowired
    VerificationTokenJPARepository verificationTokenJPARepository;

    DBVerificationTokenProvider tokenProvider;
    UserJPAMapper mapper = Mappers.getMapper(UserJPAMapper.class);

    @BeforeEach
    void init() {
        provider = new DBUserProvider(userJPARepository);
        tokenProvider = new DBVerificationTokenProvider(verificationTokenJPARepository);
    }

    @Test
    void savingAUser_saves_SocialLinks() {
        User user = ZerofiltreUtils.createMockUser(false);

        //ACT
        user = provider.save(user);

        //ASSERT
        user.getSocialLinks().forEach(socialLink -> assertThat(socialLink.getId()).isNotZero());

    }

    @Test
    void savingAUser_savesLoginFrom_asString() {
        //GIVEN
        User user = ZerofiltreUtils.createMockUser(false);

        //WHEN
        UserJPA userJPA = userJPARepository.save(mapper.toJPA(user));

        //THEN
        assertThat(SocialLink.Platform.LINKEDIN.toString()).isEqualTo(userJPA.getLoginFrom());


    }
}