package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

@RestController
@RequestMapping("/sandbox")
@AllArgsConstructor
public class SandboxController {

    private final SandboxProvider sandboxProvider;
    private final UserProvider userProvider;


    @DeleteMapping("/admin")
    public void deleteAUserSandboxOnACourse(@RequestParam long courseId, @RequestParam long userId) throws ZerofiltreException {
        User user = userProvider.userOfId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found", String.valueOf(userId)));
        sandboxProvider.destroy(user.getFullName(), ZerofiltreUtils.getValidEmail(user));

    }

    @GetMapping("/admin")
    public void createAUserSandboxForACourse(@RequestParam long courseId, @RequestParam long userId) throws ZerofiltreException {
        User user = userProvider.userOfId(userId).orElseThrow(() -> new ResourceNotFoundException("User not found", String.valueOf(userId)));
        sandboxProvider.initialize(user.getFullName(), ZerofiltreUtils.getValidEmail(user));

    }

}
