package tech.zerofiltre.blog.domain.error;

import tech.zerofiltre.blog.util.ZerofiltreUtils;

public class ZerofiltreException extends Exception {

    private final String domain;

    public ZerofiltreException(String message, String domain) {
        super(message);
        this.domain = domain;
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + " nested exception is: " + ZerofiltreUtils.getRootCauseMessage(this);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " nested exception is: " + ZerofiltreUtils.getRootCauseMessage(this);
    }

    public ZerofiltreException(String message, Throwable cause, String domain) {
        super(message, cause);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
