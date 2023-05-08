package tech.zerofiltre.blog.infra.providers.api.vimeo;

import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.retry.support.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.client.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.infra.*;

@Slf4j
@Component
public class VimeoProvider {

    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;

    public VimeoProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        this.retryTemplate = retryTemplate;
    }

    public String init(long size, String name) throws VideoUploadFailedException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "bearer " + infraProperties.getVimeoAccessToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/vnd.vimeo.*+json;version=3.4");

        String initBody = "{\n" +
                "  \"upload\": {\n" +
                "    \"approach\": \"tus\",\n" +
                "    \"size\": " + size + "\n" +
                "  },\n" +
                "  \"name\": \"" + name + "\"\n" +
                "}";

        try {
            log.info("Initializing vimeo video of size {} and name {} with request body: \n {}", size, name, initBody);
            return retryTemplate.execute(retryContext -> {
                String url = infraProperties.getVimeoRootURL() + "/me/videos";
                HttpEntity<String> requestEntity = new HttpEntity<>(initBody, headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                String result = response.getBody();
                if (result == null || result.isBlank() || result.contains("\"approach\": \"tus\"")) {
                    throw new ZerofiltreException("We could not init the video at vimeo", null);
                }
                return result;
            });
        } catch (Exception e) {
            log.error("We couldn't init the video at vimeo", e);
            throw new VideoUploadFailedException("We couldn't init the video at vimeo: " + e.getMessage(), e, "");
        }
    }
}
