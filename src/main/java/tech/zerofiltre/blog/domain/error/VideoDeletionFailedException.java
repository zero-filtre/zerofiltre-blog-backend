package tech.zerofiltre.blog.domain.error;

public class VideoDeletionFailedException extends ZerofiltreException {


    public VideoDeletionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
