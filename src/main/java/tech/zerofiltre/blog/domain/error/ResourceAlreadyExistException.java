package tech.zerofiltre.blog.domain.error;

public class ResourceAlreadyExistException extends ZerofiltreException {

    private final String uniqueIdentifier;

    public ResourceAlreadyExistException(String message, String uniqueIdentifier) {
        super(message);
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
