package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.domain.user.model.*;

import javax.validation.constraints.*;
import java.util.*;

@Data
@ToString
public class UpdateUserVM {

    @Min(value = 1, message = "The id must be greater or equal than one")
    private long id;

    @NotNull(message = "The full name must not be null")
    @NotEmpty(message = "The full name must not be empty")
    private String fullName;

    private String profilePicture;

    private String profession;

    private String bio;

    @NotNull(message = "The language must not be null")
    @NotEmpty(message = "The language must not be empty")
    private String language;

    private Set<SocialLink> socialLinks = new HashSet<>();

    private Set<String> roles = new HashSet<>(Collections.singletonList("ROLE_USER"));

    private String website;


}
