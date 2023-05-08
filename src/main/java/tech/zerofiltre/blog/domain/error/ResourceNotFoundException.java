package tech.zerofiltre.blog.domain.error;

public class ResourceNotFoundException extends ZerofiltreException {

    private final String resourceId;

    public ResourceNotFoundException(String message, String resourceId, String domain) {
        super(message, domain);
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
}
