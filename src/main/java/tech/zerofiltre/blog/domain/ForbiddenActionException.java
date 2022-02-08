package tech.zerofiltre.blog.domain;

public class ForbiddenActionException extends BlogException {

    public ForbiddenActionException(String message) {
        super(message);
    }
}
