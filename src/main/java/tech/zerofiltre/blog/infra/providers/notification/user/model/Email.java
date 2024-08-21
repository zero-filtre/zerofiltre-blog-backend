package tech.zerofiltre.blog.infra.providers.notification.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.validation.constraints.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    @NotEmpty
    @ValidEmailList
    private List<String> recipients = new ArrayList<>();
    @ValidEmailList
    private List<String> bccs = new ArrayList<>();
    @ValidEmailList
    private List<String> ccs = new ArrayList<>();
    private String content;
    private String subject;
    private String replyTo;

    private List<String> videosIds = new ArrayList<>();
    private List<String> images = new ArrayList<>();
}
