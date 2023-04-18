package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
@EqualsAndHashCode(callSuper = false)
public class UserJPA extends BaseEntityJPA {


    @Column(unique = true)
    private String pseudoName;
    @Column(unique = true)
    private String email;
    private String paymentEmail;
    private String paymentCustomerId;
    private String fullName;
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
    private String plan;


    @ElementCollection
    @Column(nullable = false)
    private Set<String> roles;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private Set<SocialLinkJPA> socialLinks;

    private String website;

    private void addSocialLink(SocialLinkJPA socialLinkJPA) {
        socialLinks.add(socialLinkJPA);
        socialLinkJPA.setUser(this);
    }

    public void setSocialLinks(Set<SocialLinkJPA> socialLinks) {
        this.socialLinks = new HashSet<>();
        if (socialLinks != null)
            socialLinks.forEach(this::addSocialLink);
    }

}
