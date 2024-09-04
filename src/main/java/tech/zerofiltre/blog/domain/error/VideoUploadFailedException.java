package tech.zerofiltre.blog.domain.error;

public class VideoUploadFailedException extends ZerofiltreException {

    public VideoUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
