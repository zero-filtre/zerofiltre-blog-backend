package tech.zerofiltre.blog.infra.security.model;

import lombok.*;

@Data
public class AuthenticationToken {

    private String header;

    private String prefix;
}
