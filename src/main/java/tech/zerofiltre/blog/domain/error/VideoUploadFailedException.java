package tech.zerofiltre.blog.domain.error;

public class VideoUploadFailedException extends ZerofiltreException {

    public VideoUploadFailedException(String message, String domain) {
        super(message, domain);
    }

    public VideoUploadFailedException(String message, Throwable cause, String domain) {
        super(message, cause, domain);
    }
}
