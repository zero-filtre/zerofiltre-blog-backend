package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RetrieveSocialTokenTest {

    public static final String TOKEN = "token";
    private RetrieveSocialToken retrieveSocialToken;

    @MockBean
    private SocialLoginProvider socialLoginProvider;

    @BeforeEach
    void init() {
        retrieveSocialToken = new RetrieveSocialToken(socialLoginProvider);

    }

    @Test
    void execute_returnsTokenProperly() throws ResourceNotFoundException {
        //ARRANGE
        when(socialLoginProvider.tokenFromCode(any())).thenReturn(TOKEN);


        //ACT
        String token = retrieveSocialToken.execute("");

        //ASSERT
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo(TOKEN);
    }

    @Test
    void execute_ThrowsNotFoundExceptionIfTokenIsNotFound() {
        //ARRANGE
        when(socialLoginProvider.tokenFromCode(any())).thenReturn(null);

        //ACT & ASSERT
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> retrieveSocialToken.execute(""));


    }


}
