package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.domain.company.model.UserCompanyInfos;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;


import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVM {

    private long id;
    private String socialId;
    private String pseudoName;
    private String email;
    private String paymentEmail;
    private String paymentCustomerId;
    private String fullName;
    private List<UserCompanyInfos> companies;
    private LocalDateTime registeredOn;
    private String profilePicture;
    private String profession;
    private String bio;
    private String language;
    private Set<SocialLink> socialLinks;
    private String website;
    private Set<String> roles;
    private boolean isActive;
    private boolean isLocked;
    private boolean isExpired;
    private boolean subscribedToBroadcast;
    private SocialLink.Platform loginFrom;
    private User.Plan plan;

    public UserInfoVM(User user){
        this.id = user.getId();
        this.socialId = user.getSocialId();
        this.pseudoName = user.getPseudoName();
        this.email = user.getEmail();
        this.paymentEmail = user.getPaymentEmail();
        this.paymentCustomerId = user.getPaymentCustomerId();
        this.fullName = user.getFullName();
        this.registeredOn = user.getRegisteredOn();
        this.profilePicture = user.getProfilePicture();
        this.profession = user.getProfession();
        this.bio = user.getBio();
        this.language = user.getLanguage();
        this.socialLinks = user.getSocialLinks();
        this.website = user.getWebsite();
        this.roles = user.getRoles();
        this.isActive = user.isActive();
        this.isLocked = user.isLocked();
        this.isExpired = user.isExpired();
        this.subscribedToBroadcast = user.isSubscribedToBroadcast();
        this.loginFrom = user.getLoginFrom();
        this.plan = user.getPlan();
    }


}
