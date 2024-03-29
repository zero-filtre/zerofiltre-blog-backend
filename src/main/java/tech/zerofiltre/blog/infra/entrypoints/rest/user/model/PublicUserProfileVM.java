package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

@Data
@ToString
public class PublicUserProfileVM {

    private String fullName;

    private String profilePicture;

    private String profession;

    private String bio;

    private String language;

    private String pseudoName;

    private LocalDateTime registeredOn;

    private Set<SocialLink> socialLinks = new HashSet<>();

    private String website;

}
