package tech.zerofiltre.blog.infra.providers.database.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class DBUserProviderIT {

    public static final String TOKEN = "token";
    public static final String SOCIAL_ID = "socialId";
    public static final String PAYMENT_EMAIL = "payment.email@zerofiltre.tech";
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
        User user = ZerofiltreUtilsTest.createMockUser(false);

        //ACT
        user = provider.save(user);

        //ASSERT
        user.getSocialLinks().forEach(socialLink -> assertThat(socialLink.getId()).isNotZero());

    }

    @Test
    void shouldGet_UserBy_PaymentEmail() {
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPaymentEmail(PAYMENT_EMAIL);

        //ACT
        provider.save(user);
        Optional<User> foundUser = provider.userOfEmail(PAYMENT_EMAIL);

        //ASSERT
        assertThat(foundUser).isNotEmpty();
        assertThat(foundUser.get().getPaymentEmail()).isEqualTo(PAYMENT_EMAIL);

    }

    @Test
    void savingAUser_with_socialId_persists_theSocialId() {
        //GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);
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
        User user = ZerofiltreUtilsTest.createMockUser(false);

        //WHEN
        UserJPA userJPA = userJPARepository.save(mapper.toJPA(user));

        //THEN
        assertThat(SocialLink.Platform.LINKEDIN.toString()).isEqualTo(userJPA.getLoginFrom());
    }

    @Test
    void listAllUsersForBroadcast() {
        //GIVEN
        User user1 = new User();
        user1.setEmail("u1@a.a");
        user1.setSubscribedToBroadcast(true);
        user1.setLanguage("fr");
        user1.setFullName("fullName1");
        userJPARepository.save(mapper.toJPA(user1));

        User user2 = new User();
        user2.setEmail("u2@a.a");
        user2.setSubscribedToBroadcast(false);
        user2.setLanguage("fr");
        user2.setFullName("fullName2");
        userJPARepository.save(mapper.toJPA(user2));

        User user3 = new User();
        user3.setPaymentEmail("u3@a.a");
        user3.setSubscribedToBroadcast(true);
        user3.setLanguage("en");
        user3.setFullName("fullName3");
        userJPARepository.save(mapper.toJPA(user3));

        //WHEN
        List<UserForBroadcast> allEmails = provider.allUsersForBroadcast();

        //THEN
        assertThat(allEmails.size()).isEqualTo(2);

        assertThat(allEmails.get(0).getEmail()).isEqualTo(user1.getEmail());
        assertThat(allEmails.get(0).getPaymentEmail()).isNull();
        assertThat(allEmails.get(0).getLanguage()).isEqualTo(user1.getLanguage());
        assertThat(allEmails.get(0).getFullName()).isEqualTo(user1.getFullName());

        assertThat(allEmails.get(1).getEmail()).isNull();
        assertThat(allEmails.get(1).getPaymentEmail()).isEqualTo(user3.getPaymentEmail());
        assertThat(allEmails.get(1).getLanguage()).isEqualTo(user3.getLanguage());
        assertThat(allEmails.get(1).getFullName()).isEqualTo(user3.getFullName());
    }

}