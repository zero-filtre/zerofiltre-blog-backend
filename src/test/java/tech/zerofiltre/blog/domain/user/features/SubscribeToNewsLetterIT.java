package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DBUserProvider.class)
class SubscribeToNewsLetterIT {

    private SubscribeToNewsLetter subscribeToNewsLetter;

    @Autowired
    UserProvider userProvider;

    @BeforeEach
    void init() {
        subscribeToNewsLetter = new SubscribeToNewsLetter(userProvider);
    }

    @Test
    @DisplayName("When a user subscribe to the news letter, then subscription is activated")
    void shouldSubscribeActivated_whenSubscribeToNewsLetter_asPlatformUser() {
        //GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setSubscribedToNewsLetter(false);
        user = userProvider.save(user);

        Optional<User> optionalUser = userProvider.userOfId(user.getId());
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().isSubscribedToNewsLetter()).isFalse();

        //WHEN
        subscribeToNewsLetter.execute(user,true);

        //THEN
        optionalUser = userProvider.userOfId(user.getId());
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().isSubscribedToNewsLetter()).isTrue();
    }

    @Test
    @DisplayName("When a user unsubscribe to the news letter, then subscription is deactivated")
    void shouldSubscribeDeactivated_whenNotSubscribeToNewsLetter_asPlatformUser() {
        //GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setSubscribedToNewsLetter(true);
        user = userProvider.save(user);

        Optional<User> optionalUser = userProvider.userOfId(user.getId());
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().isSubscribedToNewsLetter()).isTrue();

        //WHEN
        subscribeToNewsLetter.execute(user,false);

        //THEN
        optionalUser = userProvider.userOfId(user.getId());
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().isSubscribedToNewsLetter()).isFalse();
    }

}