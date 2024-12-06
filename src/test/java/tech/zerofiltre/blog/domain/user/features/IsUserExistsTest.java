package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsUserExistsTest {

    private IsUserExists isUserExists;

    @Mock
    UserProvider userProvider;

    @BeforeEach
    void init() {
        isUserExists = new IsUserExists(userProvider);
    }

    @Test
    @DisplayName("given existing user execute then return true")
    void givenExistingUser_execute_thenReturnTrue() throws ResourceNotFoundException {
        //GIVEN
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(new User()));

        //WHEN
        boolean response = isUserExists.execute(2L);

        //THEN
        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("given not existing user execute then throw resource not found exception")
    void givenNotExistingUser_execute_thenThrowResourceNotFoundException() {
        //THEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> isUserExists.execute(2L));
    }

}