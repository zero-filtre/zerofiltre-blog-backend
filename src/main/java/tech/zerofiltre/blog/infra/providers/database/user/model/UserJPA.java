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
    private String firstName;
    private String lastName;
    private LocalDateTime registeredOn;
    private String profilePicture;
    private String function;
    private String bio;
    //TODO make it embedable or use SocailLinkJPA entity
    @ElementCollection
    @Column(name="social_links", nullable=false)
    @CollectionTable(name="user_social_links", joinColumns= {@JoinColumn(name="user_id")})
    private Set<SocialLink> socialLinks;
    private String website;

}
