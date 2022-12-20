package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface ResourceProvider {
    Optional<Resource> resourceOfId(long id);
    Resource registerResource(Resource resource);
}
