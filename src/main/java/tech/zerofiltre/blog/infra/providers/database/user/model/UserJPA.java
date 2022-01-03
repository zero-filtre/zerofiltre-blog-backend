package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.*;
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
    private String firstName;
    private String lastName;
    private LocalDateTime registeredOn;
    private String profilePicture;
    private String profession;
    private String bio;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<SocialLinkJPA> socialLinks;

    private String website;

}
