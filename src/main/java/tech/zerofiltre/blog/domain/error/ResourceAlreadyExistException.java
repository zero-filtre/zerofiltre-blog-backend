package tech.zerofiltre.blog.domain.error;

public class ResourceAlreadyExistException extends BlogException {

    private final String uniqueIdentifier;

    public ResourceAlreadyExistException(String message, String uniqueIdentifier, String domain) {
        super(message, domain);
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
