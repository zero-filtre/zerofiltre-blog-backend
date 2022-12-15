package tech.zerofiltre.blog.infra.entrypoints.rest.notification;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.providers.notification.user.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.validation.*;
import javax.validation.constraints.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final BlogEmailSender emailSender;
    private final SecurityContextManager securityContextManager;
    private final InfraProperties infraProPerties;


    @PostMapping
    public String notifyByEmail(@RequestBody @Valid Email email) throws UserNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        if (!user.getRoles().contains("ROLE_ADMIN"))
            throw new ForbiddenActionException("You are not allowed to send emails", Domains.NONE.name());
        emailSender.send(email);
        return "Email(s) sent";
    }

    @PostMapping("help")
    public String notifyForHelp(@RequestParam @NotEmpty @Max(100) String subject,@RequestParam @ValidEmail String replyTo,@RequestParam @NotEmpty @Max(255) String message) throws UserNotFoundException, ForbiddenActionException {
        Email email = new Email();
        email.setRecipients(Collections.singletonList(infraProPerties.getContactEmail()));
        email.setSubject(subject);
        email.setReplyTo(replyTo);
        email.setContent(message);
        emailSender.send(email);
        return "Email sent";
    }
}
