package tech.zerofiltre.blog.domain.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Sandbox {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Sandbox{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @AllArgsConstructor
    public enum Type {
        K8S("k8s"),
        NONE("none");

        @Getter
        private final String value;

    }
}
