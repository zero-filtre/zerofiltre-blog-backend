package tech.zerofiltre.blog.infra.providers;

import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.util.*;

@Slf4j
@Component
public class GravatarProvider implements AvatarProvider {
    @Override
    public String byEmail(String email) {
        String urlPrefix = "https://www.gravatar.com/avatar/";
        String md5 = ZerofiltreUtils.md5Hex(email);
        String params = "?s=256&d=identicon&r=PG";
        return urlPrefix + md5 + params;
    }
}
