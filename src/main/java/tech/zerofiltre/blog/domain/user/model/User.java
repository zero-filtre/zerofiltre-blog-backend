package tech.zerofiltre.blog.domain.user.model;

import java.time.*;

public class User {
    private long id;
    private String pseudoName;
    private String firstName;
    private String lastName;
    private LocalDateTime registeredOn;
    private String profilePicture;


    public User() {
    }

    public User(long id, String pseudoName, String firstName, String lastName, LocalDateTime registeredOn, String profilePicture) {
        this.id = id;
        this.pseudoName = pseudoName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registeredOn = registeredOn;
        this.profilePicture = profilePicture;
    }

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
}
