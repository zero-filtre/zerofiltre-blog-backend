package tech.zerofiltre.blog.domain.user.model;

import java.util.*;

public class RegistrationCompleteEvent extends UserActionEvent {

    private boolean repeated;

    public RegistrationCompleteEvent(String appUrl, Locale locale, User user, boolean repeated) {
        super(appUrl, locale, user);
        this.repeated = repeated;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }
}
