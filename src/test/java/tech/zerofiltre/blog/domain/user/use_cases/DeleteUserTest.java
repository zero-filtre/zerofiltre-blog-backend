package tech.zerofiltre.blog.domain.user.use_cases;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class DeleteUserTest {

    @MockBean
    private UserProvider userProvider;
    private DeleteUser deleteUser;
    User currentUser = new User();
    User foundUser = new User();

    @BeforeEach
    void setUp() {
        deleteUser = new DeleteUser(userProvider);
    }

    @Test
    void deleteUser_MustDeleteViaTheUserProvider() {
        //ARRANGE
        currentUser.setRoles(Collections.singleton("ROLE_ADMIN"));
        foundUser.setId(10);
        doNothing().when(userProvider).deleteUser(any());
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(foundUser));

        //ACT & ASSERT
        Assertions.assertThatNoException().isThrownBy(() -> deleteUser.execute(currentUser, 10));
        verify(userProvider, times(1)).deleteUser(foundUser);
    }

    @Test
    void deleteUser_MustThrowExceptionOnResourceNotFound() {
        //ARRANGE
        currentUser.setRoles(Collections.singleton("ROLE_ADMIN"));
        foundUser.setId(10);
        doNothing().when(userProvider).deleteUser(any());
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> deleteUser.execute(currentUser, 10));
        verify(userProvider, times(1)).userOfId(10);
        verify(userProvider, times(0)).deleteUser(foundUser);

    }

    @Test
    @DisplayName("DeleteUser must throw ForbiddenActionException if the connected user is neither" +
            " the one to be deleted nor an administrator")
    void deleteUser_MustThrowForbiddenActionException_OnCurrentUserNotAdmin_andNotTheDeletedUser() {
        //ARRANGE
        currentUser.setRoles(Collections.singleton("ROLE_USER"));
        foundUser.setId(9);
        doNothing().when(userProvider).deleteUser(any());
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(foundUser));

        //ACT & ASSERT
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> deleteUser.execute(currentUser, 10));
        verify(userProvider, times(1)).userOfId(10);
        verify(userProvider, times(0)).deleteUser(foundUser);

    }
}