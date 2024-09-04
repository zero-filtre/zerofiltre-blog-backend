package tech.zerofiltre.blog.domain.error;

public class ForbiddenActionException extends ZerofiltreException {

    public ForbiddenActionException(String message) {
        super(message);
    }
}
