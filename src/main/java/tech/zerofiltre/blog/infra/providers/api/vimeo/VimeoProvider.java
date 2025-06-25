package tech.zerofiltre.blog.infra.providers.api.vimeo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.error.VideoUploadFailedException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.util.List;

@Slf4j
@Component
public class VimeoProvider {

    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;
    private final LessonProvider lessonProvider;

    public VimeoProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate, LessonProvider lessonProvider) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        this.retryTemplate = retryTemplate;
        this.lessonProvider = lessonProvider;
    }

    public String init(long size, String name, String lessonId) throws VideoUploadFailedException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "bearer " + infraProperties.getVimeoAccessToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/vnd.vimeo.*+json;version=3.4");

        String initBody = getVideoStructure(size, lessonId, name);

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

    public String delete(String video_id) throws ZerofiltreException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "bearer " + infraProperties.getVimeoAccessToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/vnd.vimeo.*+json;version=3.4");

        String deleteBody = video_id;

        return retryTemplate.execute(retryContext -> {
            String url = infraProperties.getVimeoRootURL() + "/me/videos";

            HttpEntity<String> requestEntity = new HttpEntity<>(deleteBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            String result = response.getBody();
            if (result == null || result.isBlank() || result.contains("\"approach\": \"tus\"")) {
                try {
                    throw new ZerofiltreException("We could not delete the video at vimeo");
                } catch (ZerofiltreException e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        });
    }

    private String getVideoStructure(long size, String lessonId, String fileName) {
        List<String> courseChapterLessonTitle = lessonProvider.getCourseChapterLessonTitle(lessonId);
        String courseName = sanitize(courseChapterLessonTitle.get(0));
        String chapterName = sanitize(courseChapterLessonTitle.get(1));
        String lessonName = sanitize(courseChapterLessonTitle.get(2));
        String structuredData = "id_" + courseName + "/id_" +  chapterName + "/id_" +  lessonName + "/" + fileName;

        String initBody = "{\n" +
                "  \"upload\": {\n" +
                "    \"approach\": \"tus\",\n" +
                "    \"size\": " + size + "\n" +
                "  },\n" +
                "  \"name\": \"" + structuredData + "\"\n" +
                "}";
        return initBody;
    }

    public String sanitize(String input){
        if(input == null) return "";
        return input
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_-]", "");
    }

}
