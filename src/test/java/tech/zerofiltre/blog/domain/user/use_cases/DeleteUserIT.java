package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBUserProvider.class, DBArticleProvider.class})
class DeleteUserIT {

    private DeleteUser deleteUser;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ArticleProvider articleProvider;

    @BeforeEach
    void init() {
        deleteUser = new DeleteUser(userProvider, articleProvider);
    }

    @Test
    @DisplayName("Deleting a user that has articles deactivates the user")
    void deleteUser_WithArticles_deactivatesHim() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User user = ZerofiltreUtils.createMockUser(false);
        userProvider.save(user);
        Article draftArticle = ZerofiltreUtils.createMockArticle(user, Collections.emptyList(), Collections.emptyList());
        articleProvider.save(draftArticle);

        //ACT
        deleteUser.execute(user, user.getId());

        Optional<User> updatedUser = userProvider.userOfId(user.getId());
        assertThat(updatedUser).isNotEmpty();
        assertThat(updatedUser.get().isExpired()).isTrue();


    }
}
