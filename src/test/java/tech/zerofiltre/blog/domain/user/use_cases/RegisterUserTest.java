package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RegisterUserTest {

    private RegisterUser registerUser;

    @MockBean
    UserProvider userProvider;

    LocalDateTime beforeRegistration = LocalDateTime.now();
    User toRegister = new User();


    @BeforeEach
    void init() {
        registerUser = new RegisterUser(userProvider);
        toRegister.setPassword("pass");
        toRegister.setLastName("last");
        toRegister.setFirstName("first");
        toRegister.setEmail("email");

    }


    @Test
    @DisplayName("Must register the user properly")
    void mustSaveProperly() throws UserAlreadyExistException {
        //ARRANGE
        when(userProvider.userOfEmail("email")).thenReturn(Optional.empty());
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> {
            User result = invocationOnMock.getArgument(0);
            result.setId(12);
            return result;
        });
        //ACT
        User registeredUser = registerUser.execute(toRegister);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userProvider, times(1)).save(any());
        assertThat(registeredUser.getFirstName()).isEqualTo(toRegister.getFirstName());
        assertThat(registeredUser.getLastName()).isEqualTo(toRegister.getLastName());
        assertThat(registeredUser.getPassword()).isEqualTo(toRegister.getPassword());
        assertThat(registeredUser.getRegisteredOn()).isNotNull();
        assertThat(registeredUser.getRoles()).contains("ROLE_USER");
        assertThat(registeredUser.getRegisteredOn()).isAfterOrEqualTo(beforeRegistration);
        assertThat(registeredUser.getId()).isNotZero();

    }

    @Test
    @DisplayName("Must throw UserAlreadyExistException if another user with the same email exist already")
    void mustCheckAlreadyExistingUsers() {
        //ARRANGE
        when(userProvider.userOfEmail("email")).thenReturn(Optional.of(toRegister));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT && ASSERT
        assertThatExceptionOfType(UserAlreadyExistException.class).isThrownBy(() -> registerUser.execute(toRegister));


    }
}