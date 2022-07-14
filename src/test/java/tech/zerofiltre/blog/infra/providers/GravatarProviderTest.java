package tech.zerofiltre.blog.infra.providers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit.jupiter.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(GravatarProvider.class)
class GravatarProviderTest {

    @Autowired
    GravatarProvider gravatarProvider;

    @Test
    void byEmail_MustReturn_ProperLink() {

        //ACT & ASSERT
        String url = gravatarProvider.byEmail("any");
        assertThat(url).contains("https://www.gravatar.com/avatar/");
        assertThat(url).contains("?s=256&d=identicon&r=PG");

    }
}