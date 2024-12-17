package tech.zerofiltre.blog.domain.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class User implements Serializable {
    private long id;
    private String socialId;
    private String pseudoName;
    private String email;
    private String paymentEmail;
    private String paymentCustomerId;
    private String fullName;
    private String password;
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
    private SocialLink.Platform loginFrom;
    private Plan plan = Plan.BASIC;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPseudoName() {
        return pseudoName;
    }

    public void setPseudoName(String pseudoName) {
        this.pseudoName = pseudoName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDateTime getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDateTime registeredOn) {
        this.registeredOn = registeredOn;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<SocialLink> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(Set<SocialLink> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public SocialLink.Platform getLoginFrom() {
        return loginFrom;
    }

    public void setLoginFrom(SocialLink.Platform loginFrom) {
        this.loginFrom = loginFrom;
    }

    public String getPaymentEmail() {
        return paymentEmail;
    }

    public void setPaymentEmail(String paymentEmail) {
        this.paymentEmail = paymentEmail;
    }

    public String getPaymentCustomerId() {
        return paymentCustomerId;
    }

    public void setPaymentCustomerId(String paymentCustomerId) {
        this.paymentCustomerId = paymentCustomerId;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", pseudoName='" + pseudoName + '\'' +
                ", email='" + email + '\'' +
                ", paymentEmail='" + paymentEmail + '\'' +
                ", paymentCustomerId='" + paymentCustomerId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", password='" + password + '\'' +
                ", registeredOn=" + registeredOn +
                ", profilePicture='" + profilePicture + '\'' +
                ", profession='" + profession + '\'' +
                ", bio='" + bio + '\'' +
                ", language='" + language + '\'' +
                ", socialLinks=" + socialLinks +
                ", website='" + website + '\'' +
                ", roles=" + roles +
                ", isActive=" + isActive +
                ", isLocked=" + isLocked +
                ", isExpired=" + isExpired +
                ", loginFrom=" + loginFrom +
                ", plan=" + plan +
                '}';
    }

    public boolean isAdmin() {
        return roles.contains("ROLE_ADMIN");
    }

    public boolean isUser() {
        return roles.contains("ROLE_USER");
    }

    public boolean isPro() {
        return plan.equals(Plan.PRO);
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String id) {
        this.socialId = id;
    }

    @AllArgsConstructor
    public enum Plan {
        BASIC("basic"),
        PRO("pro");

        @Getter
        private final String value;
    }
}
