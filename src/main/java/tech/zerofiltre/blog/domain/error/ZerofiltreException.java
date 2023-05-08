package tech.zerofiltre.blog.domain.error;

public class ZerofiltreException extends Exception {

    private final String domain;

    public ZerofiltreException(String message, String domain) {
        super(message);
        this.domain = domain;
    }

    public ZerofiltreException(String message, Throwable cause, String domain) {
        super(message, cause);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
