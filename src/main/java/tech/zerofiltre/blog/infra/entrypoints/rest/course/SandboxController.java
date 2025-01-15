package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

@RestController
@RequestMapping("/sandbox")
public class SandboxController {
    private final SandboxProvider sandboxProvider;
    private final UserProvider userProvider;

    public SandboxController(SandboxProvider sandboxProvider, UserProvider userProvider) {
        this.sandboxProvider = sandboxProvider;
        this.userProvider = userProvider;
    }

    @PostMapping("/admin")
    public Sandbox initAUserSandbox(@RequestParam long userId) throws ZerofiltreException {
        Optional<User> user = userProvider.userOfId(userId);
        if (user.isPresent())
            return sandboxProvider.initialize(user.get().getFullName(), user.get().getEmail());
        else
            throw new ResourceNotFoundException("User not found", String.valueOf(userId));
    }
}
