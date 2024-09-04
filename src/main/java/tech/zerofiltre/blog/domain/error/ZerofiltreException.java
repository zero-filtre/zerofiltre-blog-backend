package tech.zerofiltre.blog.domain.error;

public class ZerofiltreException extends Exception {


    public ZerofiltreException(String message) {
        super(message);
    }

    public ZerofiltreException(String message, Throwable cause) {
        super(message, cause);
    }

}
