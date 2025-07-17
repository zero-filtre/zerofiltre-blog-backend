package tech.zerofiltre.blog.infra.providers.api.vimeo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.ErrorCode;
import tech.zerofiltre.blog.domain.error.VideoDeletionFailedException;
import tech.zerofiltre.blog.domain.error.VideoUploadFailedException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;

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

        //String structuredName = courseName + "/" + chapterName + "/" + lessonName + "/" + name;
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
                    throw new ZerofiltreException("We could not init the video at vimeo");
                }
                return result;
            });
        } catch (Exception e) {
            log.error("We couldn't init the video at vimeo", e);
            throw new VideoUploadFailedException("We couldn't init the video at vimeo: " + e.getMessage(), e);
        }
    }

    public Object delete(String video_id) throws VideoDeletionFailedException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "bearer " + infraProperties.getVimeoAccessToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/vnd.vimeo.*+json;version=3.4");

        try {
            return retryTemplate.execute(retryContext -> {
                String url = "https://api.vimeo.com" + "/videos/" + video_id; //infraProperties.getVimeoRootURL()
                HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
                HttpStatus status = response.getStatusCode();
                if (status == HttpStatus.NO_CONTENT) {
                    log.info("Vimeo video {} deleted successfully.", video_id);
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Unexpected error when deleting Vimeo video {}: {}", video_id, e.getMessage(), e);
            throw new VideoDeletionFailedException("Unexpected error while deleting vimeo video : " + e.getMessage(), e);
        }
    }
}
