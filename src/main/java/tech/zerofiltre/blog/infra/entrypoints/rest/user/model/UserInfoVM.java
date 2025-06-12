package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.UserCompanyInfoVM;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String socialId;
    private String pseudoName;
    private String email;
    private String paymentEmail;
    private String paymentCustomerId;
    private String fullName;
    private LocalDateTime registeredOn = LocalDateTime.now();
    private String profilePicture;
    private String profession;
    private String bio;
    private String language = Locale.FRANCE.getLanguage();
    private Set<SocialLink> socialLinks = new HashSet<>();
    private String website;
    private Set<String> roles = new HashSet<>(Collections.singletonList("ROLE_USER"));
    private boolean isActive = false;
    private boolean isLocked = false;
    private boolean isExpired = false;
    private boolean subscribedToBroadcast = true;
    private SocialLink.Platform loginFrom;
    private User.Plan plan = User.Plan.BASIC;
    private List<UserCompanyInfoVM> companies = new ArrayList<>();

}
