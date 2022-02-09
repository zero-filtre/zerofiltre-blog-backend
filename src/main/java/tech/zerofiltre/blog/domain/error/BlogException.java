package tech.zerofiltre.blog.domain.error;

public class BlogException extends Exception {

    private final String domain;

    public BlogException(String message, String domain) {
        super(message);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
