package tech.zerofiltre.blog.domain.error;

public class ResourceNotFoundException extends BlogException {

    private final long resourceId;

    public ResourceNotFoundException(String message, long resourceId, String domain) {
        super(message, domain);
        this.resourceId = resourceId;
    }

    public long getResourceId() {
        return resourceId;
    }
}
