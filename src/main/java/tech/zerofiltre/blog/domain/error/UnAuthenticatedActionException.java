package tech.zerofiltre.blog.domain.error;

public class UnAuthenticatedActionException extends ZerofiltreException {

    public UnAuthenticatedActionException(String message, String domain) {
        super(message, domain);
    }
}
