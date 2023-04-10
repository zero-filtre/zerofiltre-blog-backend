package tech.zerofiltre.blog.infra.providers.api.ovh.model;


import lombok.*;

import java.util.*;

@Data
public class OVHIdentity {

    private List<String> methods;

    private OVHPassword password;

}
