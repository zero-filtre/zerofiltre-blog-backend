package tech.zerofiltre.blog.domain.error;

public class BlogException extends Exception {

    private final String domain;

    public BlogException(String message, String domain) {
        super(message);
        this.domain = domain;
    }

    public BlogException(String message, Throwable cause, String domain) {
        super(message, cause);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
