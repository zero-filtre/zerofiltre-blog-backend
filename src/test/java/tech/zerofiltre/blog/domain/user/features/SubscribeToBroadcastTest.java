package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class SubscribeToBroadcastTest {

    private SubscribeToBroadcast subscribeToBroadcast;

    @MockBean
    UserProvider userProvider;

    @BeforeEach
    void init() {
        subscribeToBroadcast = new SubscribeToBroadcast(userProvider);
    }

    @Test
    @DisplayName("When a platform user subscribe to the news letter, then subscription is activated")
    void shouldSubscribeActivated_whenSubscribeToNewsLetter_asPlatformUser() {
        //GIVEN
        User user = new User();
        user.setSubscribedToBroadcast(false);

        //WHEN
        subscribeToBroadcast.execute(user,true);

        //THEN
        assertThat(user.isSubscribedToBroadcast()).isTrue();
    }

    @Test
    @DisplayName("When a platform user unsubscribe to the news letter, then subscription is deactivated")
    void shouldSubscribeDeactivated_whenNotSubscribeToNewsLetter_asPlatformUser() {
        //GIVEN
        User user = new User();

        //WHEN
        subscribeToBroadcast.execute(user,false);

        //THEN
        assertThat(user.isSubscribedToBroadcast()).isFalse();
    }
}