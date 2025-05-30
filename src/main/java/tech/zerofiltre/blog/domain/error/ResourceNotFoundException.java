package tech.zerofiltre.blog.domain.error;

public class ResourceNotFoundException extends ZerofiltreException {

    private final String[] resourceIds;

    public ResourceNotFoundException(String message, String... resourceIds) {
        super(message);
        this.resourceIds = resourceIds;
    }

    public String[] getResourceIds() {
        return resourceIds;
    }
}
