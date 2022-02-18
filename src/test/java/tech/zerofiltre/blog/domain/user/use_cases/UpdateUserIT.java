package tech.zerofiltre.blog.domain.user.use_cases;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DatabaseUserProvider.class})
class UpdateUserIT {

    public static final String NEW_WEBSITE = "new website";
    public static final String NEW_BIO = "new bio";
    public static final String NEW_PROFESSION = "new profession";
    public static final String NEW_PROFILE_PICTURE = "new profile picture";
    public static final String NEW_LAST_NAME = "new last name";
    public static final String NEW_FIRST_NAME = "new first name";
    public static final String NEW_LINK = "new link";
    public static final String NEW_LANGUAGE = "new language";
    private UpdateUser updateUser;

    @Autowired
    private UserProvider userProvider;

    @BeforeEach
    void init() {
        updateUser = new UpdateUser(userProvider);
    }

    @Test
    void patchUser_DoesNotOverrideTheUser() throws UserNotFoundException, ForbiddenActionException {
        //ARRANGE
        User existingUser = ZerofiltreUtils.createMockUser(true);
        existingUser = userProvider.save(existingUser);

        User patchUser = new User();
        patchUser.setWebsite(NEW_WEBSITE);
        patchUser.setBio(NEW_BIO);
        patchUser.setProfession(NEW_PROFESSION);
        patchUser.setProfilePicture(NEW_PROFILE_PICTURE);
        patchUser.setFullName(NEW_FIRST_NAME);
        patchUser.setId(existingUser.getId());
        SocialLink socialLink = new SocialLink(SocialLink.Platform.STACKOVERFLOW, NEW_LINK);
        patchUser.setSocialLinks(Collections.singleton(socialLink));
        patchUser.setLanguage(NEW_LANGUAGE);

        //ACT
        patchUser = updateUser.patch(existingUser, patchUser);

        //ASSERT
        assertThat(existingUser.getId()).isNotZero();
        assertThat(existingUser.getLoginFrom()).isNotNull();
        assertThat(patchUser.getLoginFrom()).isNotNull();
        assertThat(patchUser.getWebsite()).isEqualTo(NEW_WEBSITE);
        assertThat(patchUser.getBio()).isEqualTo(NEW_BIO);
        assertThat(patchUser.getProfession()).isEqualTo(NEW_PROFESSION);
        assertThat(patchUser.getProfilePicture()).isEqualTo(NEW_PROFILE_PICTURE);
        assertThat(patchUser.getFullName()).isEqualTo(NEW_FIRST_NAME);
        assertThat(patchUser.getLanguage()).isEqualTo(NEW_LANGUAGE);
        Set<SocialLink> socialLinks = patchUser.getSocialLinks();
        assertThat(socialLinks.size()).isEqualTo(1);
        assertThat(socialLinks.stream().anyMatch(link ->
                link.getLink().equals(NEW_LINK) && link.getPlatform().equals(SocialLink.Platform.STACKOVERFLOW)
        )).isTrue();

    }
}
