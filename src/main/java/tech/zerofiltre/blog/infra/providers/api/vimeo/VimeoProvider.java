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
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.util.DataChecker;

@Slf4j
@Component
public class VimeoProvider {

    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;
    private final DataChecker dataChecker;

    public VimeoProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate, DataChecker dataChecker, SecurityContextManager securityContextManager) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        this.retryTemplate = retryTemplate;
        this.dataChecker = dataChecker;
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
                    throw new ZerofiltreException("We could not init the video at vimeo");
                }
                return result;
            });
        } catch (Exception e) {
            log.error("We couldn't init the video at vimeo", e);
            throw new VideoUploadFailedException("We couldn't init the video at vimeo: " + e.getMessage(), e);
        }
    }

    public String delete(long courseId, String video_id, User currentUser) throws VideoDeletionFailedException, ForbiddenActionException, ResourceNotFoundException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "bearer " + infraProperties.getVimeoAccessToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/vnd.vimeo.*+json;version=3.4");
        if(currentUser.isAdmin()|| dataChecker.isVideoOwner(courseId, currentUser)){
            try {
                 return retryTemplate.execute(retryContext -> {
                    String url = infraProperties.getVimeoRootURL() + "/videos/" + video_id;
                    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
                    HttpStatus status = response.getStatusCode();
                    if (status == HttpStatus.NO_CONTENT) {
                        log.info("Vimeo video {} deleted successfully.", video_id);
                    }
                    return response.getStatusCode().toString();
                });
            } catch (Exception e) {
                log.error("Unexpected error when deleting Vimeo video {}: {}", video_id, e);
                throw new VideoDeletionFailedException("Unexpected error while deleting vimeo video : ", e);
            }
        }else{
            throw new ForbiddenActionException("You don't have the right to delete this video !");
        }


    }
}
