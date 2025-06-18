package tech.zerofiltre.blog.infra.entrypoints.rest.video;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.providers.api.vimeo.*;

@Slf4j
@RestController
@RequestMapping("/vimeo")
@RequiredArgsConstructor
public class VimeoController {

    private final VimeoProvider vimeoService;
    private final SecurityContextManager securityContextManager;

    @PostMapping("/init")
    public String init(@RequestParam long size, @RequestParam String name) throws VideoUploadFailedException {
        return vimeoService.init(size, name);
    }
    @DeleteMapping("/{courseId}/{videoId}")
    public void deleteVideo(@PathVariable("courseId") long courseId, @PathVariable("videoId") String videoId) throws ZerofiltreException {
        User currentUser = securityContextManager.getAuthenticatedUser();
        vimeoService.delete(courseId, videoId, currentUser);
    }
}
