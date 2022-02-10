package tech.zerofiltre.blog.infra.providers.api.so;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.json.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.retry.support.*;
import org.springframework.test.web.client.*;
import org.springframework.web.client.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.api.config.*;

import java.net.*;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@JsonTest //I don't know why @RunWith(SpringExtension.class) is not working
@Import({InfraProperties.class, APIClientConfiguration.class,InfraConfiguration.class})
class StackOverflowLoginProviderTest {

    private static final String TOKEN = "token";

    private final String checkTokenUri = "https://api.stackexchange.com/2.3/access-tokens/token?key=ZAeo5W0MnZPxiEBgb99MvA((";
    private final String getUserInfoURI = "https://api.stackexchange.com/2.3/me?access_token=token&filter=default&site=stackoverflow&sort=reputation&key=ZAeo5W0MnZPxiEBgb99MvA((&order=desc";

    private SocialLoginProvider stackOverflowProvider;

    private MockRestServiceServer mockServer;

    @Autowired
    private InfraProperties infraProperties;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private String tokenResponse;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        stackOverflowProvider = new StackOverflowLoginProvider(restTemplate, infraProperties, retryTemplate);
    }

    @Test
    void isValid_returnsTrue_onValidToken() throws URISyntaxException {
        long tomorrow = LocalDateTime.now().plusHours(24).toEpochSecond(ZoneOffset.UTC);


        //ARRANGE
        tokenResponse = "{\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"scope\": [\n" +
                "                \"read_inbox\"\n" +
                "            ],\n" +
                "            \"account_id\": 7377225,\n" +
                "            \"expires_on_date\": " + tomorrow + ",\n" +
                "            \"access_token\": \"kata\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"has_more\": false,\n" +
                "    \"quota_max\": 10000,\n" +
                "    \"quota_remaining\": 9974\n" +
                "}";

        //ACT
        boolean isValid = checkIfValid(checkTokenUri);

        //ASSERT
        assertThat(isValid).isTrue();

    }

    @Test
    void isValid_returnsTrue_onItemsMissing() throws URISyntaxException {

        //ARRANGE
        tokenResponse = "{\n" +
                "    \"items\": [],\n" +
                "    \"has_more\": false,\n" +
                "    \"quota_max\": 10000,\n" +
                "    \"quota_remaining\": 9973\n" +
                "}";

        //ACT
        boolean isValid = checkIfValid(checkTokenUri);

        //ASSERT
        assertThat(isValid).isFalse();

    }


    @Test
    void isValid_returnsFalse_onExpiredToken() throws URISyntaxException {

        //ARRANGE
        tokenResponse = "{\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"scope\": [\n" +
                "                \"read_inbox\"\n" +
                "            ],\n" +
                "            \"account_id\": 7377225,\n" +
                "            \"expires_on_date\": 1643273099,\n" +
                "            \"access_token\": \"kata\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"has_more\": false,\n" +
                "    \"quota_max\": 10000,\n" +
                "    \"quota_remaining\": 9974\n" +
                "}";

        //ACT
        boolean isValid = checkIfValid(checkTokenUri);

        //ASSERT
        assertThat(isValid).isFalse();

    }

    @Test
    void isValid_isFalse_onResponseError() throws URISyntaxException {
        //ARRANGE

        //ACT
        sendMockRequest(checkTokenUri, "", HttpStatus.BAD_REQUEST);
        boolean isValid = stackOverflowProvider.isValid(TOKEN);

        //ASSERT
        assertThat(isValid).isFalse();
    }

    @Test
    void userOfToken_isEmpty_onResponseError() throws URISyntaxException {
        //ARRANGE

        //ACT
        sendMockRequest(getUserInfoURI, "", HttpStatus.BAD_REQUEST);
        Optional<User> userOptional = stackOverflowProvider.userOfToken(TOKEN);

        //ASSERT
        assertThat(userOptional).isEmpty();


    }

    @Test
    void userOfToken_returnsAUser_onValidInput() throws URISyntaxException {
        //ARRANGE
        String getUserInfoResponse = "{\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"badge_counts\": {\n" +
                "                \"bronze\": 23,\n" +
                "                \"silver\": 11,\n" +
                "                \"gold\": 1\n" +
                "            },\n" +
                "            \"account_id\": 99999,\n" +
                "            \"is_employee\": false,\n" +
                "            \"last_modified_date\": 1614901200,\n" +
                "            \"last_access_date\": 1643530325,\n" +
                "            \"reputation_change_year\": 0,\n" +
                "            \"reputation_change_quarter\": 0,\n" +
                "            \"reputation_change_month\": 0,\n" +
                "            \"reputation_change_week\": 0,\n" +
                "            \"reputation_change_day\": 0,\n" +
                "            \"reputation\": 815,\n" +
                "            \"creation_date\": 1448720877,\n" +
                "            \"user_type\": \"registered\",\n" +
                "            \"user_id\": 8787878,\n" +
                "            \"accept_rate\": 58,\n" +
                "            \"location\": \"Paris, France\",\n" +
                "            \"website_url\": \"https://zerofiltre.tech\",\n" +
                "            \"link\": \"https://stackoverflow.com/users/8787878/philippe-simo\",\n" +
                "            \"profile_image\": \"https://www.gravatar.com/avatar/30bb65fba474796ea67d26cfe0a501bc?s=256&d=identicon&r=PG&f=1\",\n" +
                "            \"display_name\": \"Philippe Simo\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"has_more\": false,\n" +
                "    \"quota_max\": 10000,\n" +
                "    \"quota_remaining\": 9996\n" +
                "}";

        //ACT
        sendMockRequest(getUserInfoURI, getUserInfoResponse, HttpStatus.OK);
        Optional<User> userOptional = stackOverflowProvider.userOfToken(TOKEN);

        //ASSERT
        assertThat(userOptional).isNotEmpty();
        User user = userOptional.get();
        assertThat(user.getLanguage()).isEqualTo(Locale.FRANCE.getLanguage());
        assertThat(user.getEmail()).isEqualTo("8787878");
        assertThat(user.getProfilePicture()).isEqualTo("https://www.gravatar.com/avatar/30bb65fba474796ea67d26cfe0a501bc?s=256&d=identicon&r=PG&f=1");
        assertThat(user.getFirstName()).isEqualTo("Philippe Simo");
        assertThat(user.getWebsite()).isEqualTo("https://zerofiltre.tech");
        assertThat(user.getLoginFrom()).isEqualTo(SocialLink.Platform.STACKOVERFLOW);
        user.getSocialLinks().forEach(socialLink -> {
            assertThat(socialLink.getPlatform()).isEqualTo(SocialLink.Platform.STACKOVERFLOW);
            assertThat(socialLink.getLink()).isEqualTo("https://stackoverflow.com/users/8787878/philippe-simo");
        });
        assertThat(user.getRegisteredOn()).isBeforeOrEqualTo(LocalDateTime.now());


    }

    private boolean checkIfValid(String uri) throws URISyntaxException {
        sendMockRequest(uri, tokenResponse, HttpStatus.OK);
        //ACT
        return stackOverflowProvider.isValid(TOKEN);
    }

    private void sendMockRequest(String uri, String response, HttpStatus status) throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(uri)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response)
                );
    }

}