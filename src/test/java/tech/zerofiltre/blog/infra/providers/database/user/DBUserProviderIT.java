package tech.zerofiltre.blog.infra.providers.database.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class DBUserProviderIT {

    public static final String TOKEN = "token";
    public static final String SOCIAL_ID = "socialId";
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
    void savingAUser_with_socialId_persists_theSocialId() {
        //GIVEN
        User user = ZerofiltreUtils.createMockUser(false);
        user.setSocialId(SOCIAL_ID);

        //WHEN
        UserJPA userJPA = userJPARepository.save(mapper.toJPA(user));

        UserJPA foundUserJPA = userJPARepository.findBySocialId(SOCIAL_ID).get();

        //THEN
        assertThat(foundUserJPA.getSocialId()).isEqualTo(SOCIAL_ID);
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