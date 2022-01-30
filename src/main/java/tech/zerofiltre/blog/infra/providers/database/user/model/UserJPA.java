package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;
import java.time.*;
import java.util.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "user")
public class UserJPA extends BaseEntityJPA {

    private String pseudoName;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private LocalDateTime registeredOn;
    private String profilePicture;
    private String profession;
    private String bio;
    private String language;
    private boolean isActive = false;
    private boolean isLocked = false;
    private boolean isExpired = false;
    private SocialLink.Platform loginFrom;


    @ElementCollection
    @Column(nullable = false)
    private Set<String> roles;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<SocialLinkJPA> socialLinks;

    private String website;

}
