package tech.zerofiltre.blog.domain.error;

public class ForbiddenActionException extends BlogException {

    public ForbiddenActionException(String message, String domain) {
        super(message, domain);
    }
}
