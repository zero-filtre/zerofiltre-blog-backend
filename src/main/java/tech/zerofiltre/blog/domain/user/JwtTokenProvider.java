package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.*;

public interface JwtTokenProvider {

    JwtToken generate(User user);
}
