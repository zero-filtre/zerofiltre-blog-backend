package tech.zerofiltre.blog.infra.providers.api.ovh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.api.ovh.model.OVHToken;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OVHTokenProviderTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    InfraProperties infraProperties;
    @Mock
    RetryTemplate retryTemplate;
    private OVHTokenProvider ovhTokenProvider;

    @BeforeEach
    void setUp() {
        when(infraProperties.getOvhAuthUrl()).thenReturn("/path");
        ovhTokenProvider = new OVHTokenProvider(restTemplate, infraProperties, retryTemplate);
    }

    @Test
    @DisplayName("When the token has not been found, I return a valid token.")
    void ShouldReturnValidToken_whenGetToken_forNotFoundToken() throws ZerofiltreException {
        //GIVEN
        String tokenBody = "{\n" +
                "    \"token\": {\n" +
                "        \"expires_at\": \"2024-04-30T00:00:00.000000Z\"\n" +
                "    }\n" +
                "}";

        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
        values.add("x-subject-token", "accesstokenAAABBB000111");
        HttpHeaders headers = new HttpHeaders(values);

        ResponseEntity<String> responseRestTemplate = new ResponseEntity<>(tokenBody, headers, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenReturn(responseRestTemplate);

        // Mock du retryTemplate pour exécuter la lambda passée
        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<OVHToken, Exception> callback = invocation.getArgument(0);
            return callback.doWithRetry(null);
        });

        OVHToken tokenExpected = new OVHToken();
        tokenExpected.setAccessToken("accesstokenAAABBB000111");
        tokenExpected.setExpiresAt("2024-04-30T00:00:00.000000Z");

        //WHEN
        OVHToken response = ovhTokenProvider.getToken();

        //THEN
        assertThat(response).isEqualTo(tokenExpected);
    }

    @Test
    @DisplayName("When the token has been found and is expired, I return a valid token.")
    void ShouldReturnNewToken_whenGetToken_forExpiredToken() throws ZerofiltreException {
        //GIVEN
        String tokenBody = "{\n" +
                "    \"token\": {\n" +
                "        \"expires_at\": \"2024-04-30T00:00:00.000000Z\"\n" +
                "    }\n" +
                "}";

        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
        values.add("x-subject-token", "accesstokenAAABBB000111");
        HttpHeaders headers = new HttpHeaders(values);

        ResponseEntity<String> responseRestTemplate = new ResponseEntity<>(tokenBody, headers, HttpStatus.OK);

        OVHToken expiredToken = new OVHToken();
        expiredToken.setAccessToken("expiredaccesstokenAAABBB000111");
        expiredToken.setExpiresAt("2024-04-22T00:00:00.000000Z");

        ReflectionTestUtils.setField(ovhTokenProvider, "token", expiredToken);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenReturn(responseRestTemplate);

        // Mock du retryTemplate pour exécuter la lambda passée
        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<OVHToken, Exception> callback = invocation.getArgument(0);
            return callback.doWithRetry(null);
        });

        OVHToken tokenExpected = new OVHToken();
        tokenExpected.setAccessToken("accesstokenAAABBB000111");
        tokenExpected.setExpiresAt("2024-04-30T00:00:00.000000Z");

        //WHEN
        OVHToken response = ovhTokenProvider.getToken();

        //THEN
        assertThat(response).isEqualTo(tokenExpected);
    }

    @Test
    @DisplayName("When the token has been found and is not expired, I return the same token.")
    void ShouldReturnSameToken_whenGetToken_forNotExpiredToken() throws ZerofiltreException {
        //GIVEN
        OVHToken notExpiredToken = new OVHToken();
        notExpiredToken.setAccessToken("expiredaccesstokenAAABBB000111");
        notExpiredToken.setExpiresAt(ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ReflectionTestUtils.setField(ovhTokenProvider, "token", notExpiredToken);

        //WHEN
        OVHToken response = ovhTokenProvider.getToken();

        //THEN
        assertThat(response).isEqualTo(notExpiredToken);
    }

    @Test
    @DisplayName("When the application cannot contact OVH to receive a token, the ZerofiltreException is thrown.")
    void ShouldReturnException_whenGetToken_andCannotContactOVH() {
        //GIVEN
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenThrow(RestClientException.class);

        // Mock du retryTemplate pour exécuter la lambda passée
        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<OVHToken, Exception> callback = invocation.getArgument(0);
            return callback.doWithRetry(null);
        });

        //THEN
        assertThatExceptionOfType(ZerofiltreException.class)
                .isThrownBy(() -> ovhTokenProvider.getToken());
    }
}