package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

@Data
public class PublicUserProfileVM {

    private String fullName;

    private String profilePicture;

    private String profession;

    private String bio;

    private String language;

    private Set<SocialLink> socialLinks = new HashSet<>();

    private String website;

}
