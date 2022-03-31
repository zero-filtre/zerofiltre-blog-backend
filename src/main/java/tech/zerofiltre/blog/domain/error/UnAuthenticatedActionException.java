package tech.zerofiltre.blog.domain.error;

public class UnAuthenticatedActionException extends BlogException {

    public UnAuthenticatedActionException(String message, String domain) {
        super(message, domain);
    }
}
