package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@Import(UserDatabaseProvider.class)
class RegisterUserIT {

    private RegisterUser registerUser;

    @Autowired
    UserProvider userProvider;


    User toRegister = new User();

    @BeforeEach
    void init() {
        registerUser = new RegisterUser(userProvider);
        toRegister.setPassword("pass");
        toRegister.setLastName("last");
        toRegister.setFirstName("first");

    }


    @Test
    @DisplayName("Must register the user properly")
    void mustSaveProperly() throws UserAlreadyExistException {
        //ARRANGE
        toRegister.setEmail("email");

        //ACT
        User registeredUser = registerUser.execute(toRegister);

        //ASSERT
        assertThat(registeredUser.getFirstName()).isEqualTo(toRegister.getFirstName());
        assertThat(registeredUser.getLastName()).isEqualTo(toRegister.getLastName());
        assertThat(registeredUser.getPassword()).isEqualTo(toRegister.getPassword());
        assertThat(registeredUser.getRoles()).contains("ROLE_USER");
        assertThat(registeredUser.getId()).isNotZero();

    }

    @Test
    @DisplayName("Must throw UserAlreadyExistException if another user with the same email exist already")
    void mustCheckAlreadyExistingUsers() {
        //ARRANGE
        toRegister.setEmail("email1");

        userProvider.save(toRegister);

        //ACT && ASSERT
        assertThatExceptionOfType(UserAlreadyExistException.class).isThrownBy(() -> registerUser.execute(toRegister));


    }
}