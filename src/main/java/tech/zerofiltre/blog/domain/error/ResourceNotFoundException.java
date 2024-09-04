package tech.zerofiltre.blog.domain.error;

public class ResourceNotFoundException extends ZerofiltreException {

    private final String resourceId;

    public ResourceNotFoundException(String message, String resourceId) {
        super(message);
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
}
