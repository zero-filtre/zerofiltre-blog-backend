package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private String loginFrom;
    private String plan;


    @ElementCollection
    @Column(nullable = false)
    private Set<String> roles;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private Set<SocialLinkJPA> socialLinks;

    private String website;
    private String socialId;

    private void addSocialLink(SocialLinkJPA socialLinkJPA) {
        socialLinks.add(socialLinkJPA);
        socialLinkJPA.setUser(this);
    }

    public void setSocialLinks(Set<SocialLinkJPA> socialLinks) {
        this.socialLinks = new HashSet<>();
        if (socialLinks != null)
            socialLinks.forEach(this::addSocialLink);
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }
}
