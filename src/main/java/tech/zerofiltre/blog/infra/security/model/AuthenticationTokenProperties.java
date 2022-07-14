package tech.zerofiltre.blog.infra.security.model;

import lombok.*;

@Data
public class AuthenticationTokenProperties {

    private String header;

    private String prefix;
}
