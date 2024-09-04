package tech.zerofiltre.blog.infra.entrypoints.rest.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.use_cases.UserNotFoundException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.providers.notification.user.ZerofiltreEmailSender;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.ValidEmail;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final ZerofiltreEmailSender emailSender;
    private final SecurityContextManager securityContextManager;
    private final InfraProperties infraProPerties;


    @PostMapping
    public String notifyByEmail(@RequestBody @Valid Email email) throws UserNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        if (!user.getRoles().contains("ROLE_ADMIN"))
            throw new ForbiddenActionException("You are not allowed to send emails");
        emailSender.send(email, false);
        return "Email(s) sent";
    }

    @PostMapping("help")
    public String notifyForHelp(@RequestParam @NotEmpty @Max(100) String subject, @RequestParam @ValidEmail String replyTo, @RequestParam @NotEmpty @Max(255) String message) {
        Email email = new Email();
        email.setRecipients(Collections.singletonList(infraProPerties.getContactEmail()));
        email.setSubject(subject);
        email.setReplyTo(replyTo);
        email.setContent(message);
        emailSender.send(email, true);
        return "Email sent";
    }

    @PostMapping("all")
    public String notifyByEmailForAllUsers(@RequestBody @Valid Email email) throws UserNotFoundException, ForbiddenActionException {
            User user = securityContextManager.getAuthenticatedUser();
            if (!user.getRoles().contains("ROLE_ADMIN"))
                throw new ForbiddenActionException("You are not allowed to send emails");
        emailSender.sendForAllUsers(email);
        return "Email(s) sent for all users";
    }
}
