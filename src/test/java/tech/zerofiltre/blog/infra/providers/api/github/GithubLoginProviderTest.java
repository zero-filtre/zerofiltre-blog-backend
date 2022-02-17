package tech.zerofiltre.blog.infra.providers.api.github;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.json.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.retry.support.*;
import org.springframework.test.web.client.*;
import org.springframework.web.client.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;

import java.net.*;
import java.nio.charset.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@JsonTest //I don't know why @RunWith(SpringExtension.class) is not working
@Import({InfraProperties.class, APIClientConfiguration.class, InfraConfiguration.class})
class GithubLoginProviderTest {

    public static final String ACCESS_CODE = "code";
    public static final String ACCESS_TOKEN = "gho_16C7e42F292c6912E7710c838347Ae178B4a";

    @Value("${zerofiltre.infra.api.github.client-id}")
    String clientId;

    @Value("${zerofiltre.infra.api.github.client-secret}")
    String clientSecret;

    private MockRestServiceServer mockServer;
    private static final String TOKEN = "token";
    private final String getUserInfoUri = "https://api.github.com/user";


    private GithubLoginProvider githubLoginProvider;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InfraProperties infraProperties;

    @Autowired
    private RetryTemplate retryTemplate;

    private String checkTokenUri;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        githubLoginProvider = new GithubLoginProvider(restTemplate, infraProperties, retryTemplate);
        checkTokenUri = "https://api.github.com/applications/" + clientId + "/token";

    }

    @Test
    void getTokenFromCode_returnsAValidToken() throws URISyntaxException, ResourceNotFoundException {
        //ARRANGE
        String response = "{\n" +
                "  \"access_token\": \"" + ACCESS_TOKEN + "\",\n" +
                "  \"scope\":\"repo,gist\",\n" +
                "  \"token_type\":\"bearer\"\n" +
                "}";

        String getTokenFromCodeURI = "https://github.com/login/oauth/access_token?code=" + ACCESS_CODE + "&client_id="
                + clientId + "&client_secret=" + clientSecret;
        prepareMockRequest(getTokenFromCodeURI, response, HttpStatus.OK, HttpMethod.POST, false, false, true);

        //ACT
        String token = githubLoginProvider.tokenFromCode(ACCESS_CODE);

        //ASSERT
        assertThat(token).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    void isValid_returnsTrue_onValidToken() throws URISyntaxException {

        //ARRANGE
        prepareMockRequest(checkTokenUri, "", HttpStatus.OK, HttpMethod.POST, true, false, false);

        //ACT
        boolean isValid = githubLoginProvider.isValid(TOKEN);

        //ASSERT
        assertThat(isValid).isTrue();
    }

    @Test
    void isValid_returnsFalse_onResponseError() throws URISyntaxException {

        //ARRANGE
        prepareMockRequest(checkTokenUri, "", HttpStatus.NOT_FOUND, HttpMethod.POST, true, false, false);

        //ACT
        boolean isValid = githubLoginProvider.isValid(TOKEN);

        //ASSERT
        assertThat(isValid).isFalse();
    }

    @Test
    void userOfToken_buildUserWithAlternativesDataProperly() throws URISyntaxException {
        //ARRANGE
        String response = "{\n" +
                "    \"login\": \"login\",\n" +
                "    \"id\": 45454545454,\n" +
                "    \"node_id\": \"xxxxxxxxxxxxxxxxxx\",\n" +
                "    \"avatar_url\": \"https://avatars.githubusercontent.com/u/13754910?v=4\",\n" +
                "    \"gravatar_id\": \"\",\n" +
                "    \"url\": \"https://api.github.com/users/login\",\n" +
                "    \"html_url\": \"https://github.com/login\",\n" +
                "    \"followers_url\": \"https://api.github.com/users/login/followers\",\n" +
                "    \"following_url\": \"https://api.github.com/users/login/following{/other_user}\",\n" +
                "    \"gists_url\": \"https://api.github.com/users/login/gists{/gist_id}\",\n" +
                "    \"starred_url\": \"https://api.github.com/users/login/starred{/owner}{/repo}\",\n" +
                "    \"subscriptions_url\": \"https://api.github.com/users/login/subscriptions\",\n" +
                "    \"organizations_url\": \"https://api.github.com/users/login/orgs\",\n" +
                "    \"repos_url\": \"https://api.github.com/users/login/repos\",\n" +
                "    \"events_url\": \"https://api.github.com/users/login/events{/privacy}\",\n" +
                "    \"received_events_url\": \"https://api.github.com/users/login/received_events\",\n" +
                "    \"type\": \"User\",\n" +
                "    \"site_admin\": false,\n" +
                "    \"name\": null,\n" +
                "    \"company\": null,\n" +
                "    \"blog\": \"https://zerofiltre.tech\",\n" +
                "    \"location\": null,\n" +
                "    \"email\": \"optimium@gmail.com\",\n" +
                "    \"hireable\": null,\n" +
                "    \"bio\": null,\n" +
                "    \"twitter_username\": null,\n" +
                "    \"public_repos\": 40,\n" +
                "    \"public_gists\": 33,\n" +
                "    \"followers\": 11,\n" +
                "    \"following\": 1,\n" +
                "    \"created_at\": \"2015-08-11T20:51:21Z\",\n" +
                "    \"updated_at\": \"2022-01-31T17:51:36Z\",\n" +
                "    \"private_gists\": 0,\n" +
                "    \"total_private_repos\": 1,\n" +
                "    \"owned_private_repos\": 1,\n" +
                "    \"disk_usage\": 376970,\n" +
                "    \"collaborators\": 0,\n" +
                "    \"two_factor_authentication\": false,\n" +
                "    \"plan\": {\n" +
                "        \"name\": \"free\",\n" +
                "        \"space\": 121211111,\n" +
                "        \"collaborators\": 0,\n" +
                "        \"private_repos\": 10000\n" +
                "    }\n" +
                "}";
        prepareMockRequest(getUserInfoUri, response, HttpStatus.OK, HttpMethod.GET, false, true, false);

        //ACT
        Optional<User> optionalUser = githubLoginProvider.userOfToken(TOKEN);

        //ASSERT
        assertThat(optionalUser).isNotEmpty();
        User user = optionalUser.get();
        assertThat(user.getEmail()).isEqualTo("optimium@gmail.com");
        assertThat(user.getFullName()).isEqualTo("Login");
        assertThat(user.getProfilePicture()).isEqualTo("https://avatars.githubusercontent.com/u/13754910?v=4");
        assertThat(user.getBio()).isNull();
        assertThat(user.getWebsite()).isEqualTo("https://zerofiltre.tech");
        assertThat(user.getLoginFrom()).isEqualTo(SocialLink.Platform.GITHUB);
        Set<SocialLink> socialLinks = user.getSocialLinks();
        assertThat(socialLinks.size()).isEqualTo(1);
        socialLinks.forEach(socialLink -> {
                    assertThat(socialLink.getPlatform()).isEqualTo(SocialLink.Platform.GITHUB);
                    assertThat(socialLink.getLink()).isEqualTo("https://github.com/login");
                }
        );
    }

    @Test
    void userOfToken_buildUserProperly() throws URISyntaxException {
        //ARRANGE
        String response = "{\n" +
                "    \"login\": \"login\",\n" +
                "    \"id\": 45454545454,\n" +
                "    \"node_id\": \"xxxxxxxxxxxxxxxxxx\",\n" +
                "    \"avatar_url\": \"https://avatars.githubusercontent.com/u/13754910?v=4\",\n" +
                "    \"gravatar_id\": \"\",\n" +
                "    \"url\": \"https://api.github.com/users/login\",\n" +
                "    \"html_url\": \"https://github.com/login\",\n" +
                "    \"followers_url\": \"https://api.github.com/users/login/followers\",\n" +
                "    \"following_url\": \"https://api.github.com/users/login/following{/other_user}\",\n" +
                "    \"gists_url\": \"https://api.github.com/users/login/gists{/gist_id}\",\n" +
                "    \"starred_url\": \"https://api.github.com/users/login/starred{/owner}{/repo}\",\n" +
                "    \"subscriptions_url\": \"https://api.github.com/users/login/subscriptions\",\n" +
                "    \"organizations_url\": \"https://api.github.com/users/login/orgs\",\n" +
                "    \"repos_url\": \"https://api.github.com/users/login/repos\",\n" +
                "    \"events_url\": \"https://api.github.com/users/login/events{/privacy}\",\n" +
                "    \"received_events_url\": \"https://api.github.com/users/login/received_events\",\n" +
                "    \"type\": \"User\",\n" +
                "    \"site_admin\": false,\n" +
                "    \"name\": \"Watson ONANA\",\n" +
                "    \"company\": null,\n" +
                "    \"blog\": \"https://zerofiltre.tech\",\n" +
                "    \"location\": null,\n" +
                "    \"email\": null,\n" +
                "    \"hireable\": null,\n" +
                "    \"bio\": null,\n" +
                "    \"twitter_username\": \"login\",\n" +
                "    \"public_repos\": 40,\n" +
                "    \"public_gists\": 33,\n" +
                "    \"followers\": 11,\n" +
                "    \"following\": 1,\n" +
                "    \"created_at\": \"2015-08-11T20:51:21Z\",\n" +
                "    \"updated_at\": \"2022-01-31T17:51:36Z\",\n" +
                "    \"private_gists\": 0,\n" +
                "    \"total_private_repos\": 1,\n" +
                "    \"owned_private_repos\": 1,\n" +
                "    \"disk_usage\": 376970,\n" +
                "    \"collaborators\": 0,\n" +
                "    \"two_factor_authentication\": false,\n" +
                "    \"plan\": {\n" +
                "        \"name\": \"free\",\n" +
                "        \"space\": 121211111,\n" +
                "        \"collaborators\": 0,\n" +
                "        \"private_repos\": 10000\n" +
                "    }\n" +
                "}";
        prepareMockRequest(getUserInfoUri, response, HttpStatus.OK, HttpMethod.GET, false, true, false);

        //ACT
        Optional<User> optionalUser = githubLoginProvider.userOfToken(TOKEN);

        //ASSERT
        assertThat(optionalUser).isNotEmpty();
        User user = optionalUser.get();
        assertThat(user.getEmail()).isEqualTo("login");
        assertThat(user.getFullName()).isEqualTo("Watson ONANA");
        assertThat(user.getProfilePicture()).isEqualTo("https://avatars.githubusercontent.com/u/13754910?v=4");
        assertThat(user.getBio()).isNull();
        assertThat(user.getWebsite()).isEqualTo("https://zerofiltre.tech");
        assertThat(user.getLoginFrom()).isEqualTo(SocialLink.Platform.GITHUB);
        Set<SocialLink> socialLinks = user.getSocialLinks();
        assertThat(socialLinks.size()).isEqualTo(2);
        assertThat(socialLinks.stream().allMatch(socialLink ->
                (socialLink.getPlatform().equals(SocialLink.Platform.GITHUB) ||
                        socialLink.getPlatform().equals(SocialLink.Platform.TWITTER)) &&
                        (socialLink.getLink().equals("https://github.com/login") ||
                                socialLink.getLink().equals("https://twitter.com/login"))
        )).isTrue();
    }

    private void prepareMockRequest(String uri, String response, HttpStatus status, HttpMethod expectedMethod, boolean withBasicAuth, boolean withTokenAuth, boolean withAcceptJson) throws URISyntaxException {


        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        ResponseActions responseActions = mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(uri)))
                .andExpect(method(expectedMethod));
        if (withBasicAuth) {
            responseActions.andExpect(header(HttpHeaders.AUTHORIZATION, authHeader));
        }
        if (withTokenAuth) {
            responseActions.andExpect(header(HttpHeaders.AUTHORIZATION, "token " + TOKEN));
        }
        if (withAcceptJson)
            responseActions.andExpect(header(HttpHeaders.ACCEPT, "application/json"));
        else
            responseActions.andExpect(header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json"));


        responseActions.andRespond(withStatus(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response)
        );
    }
}