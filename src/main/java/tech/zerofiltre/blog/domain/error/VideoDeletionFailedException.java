package tech.zerofiltre.blog.domain.error;

public class VideoDeletionFailedException extends ZerofiltreException {

    private final ErrorCode errorCode;

    public VideoDeletionFailedException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
