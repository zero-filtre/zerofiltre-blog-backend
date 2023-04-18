package tech.zerofiltre.blog.infra.providers.api.ovh.model;

import lombok.*;


@Data
public class OVHUser {
    private String name;
    private OVHDomain domain;
    private String password;
}
