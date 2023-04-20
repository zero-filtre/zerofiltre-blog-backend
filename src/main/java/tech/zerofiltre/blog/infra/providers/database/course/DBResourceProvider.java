package tech.zerofiltre.blog.infra.providers.database.course;

import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;

import java.util.*;

@Component
public class DBResourceProvider implements ResourceProvider {

    private final ResourceJPARepository resourceRepository;
    private final ResourceJPAMapper resourceJPAMapper = Mappers.getMapper(ResourceJPAMapper.class);

    public DBResourceProvider(ResourceJPARepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Optional<Resource> resourceOfId(long id) {
        return resourceRepository.findById(id).map(resourceJPAMapper::fromJPA);
    }

    @Override
    public Resource save(Resource resource) {
        return resourceJPAMapper.fromJPA(resourceRepository.save(resourceJPAMapper.toJPA(resource)));
    }

    @Override
    public void delete(long resourceId) {
        resourceRepository.deleteById(resourceId);

    }
}
