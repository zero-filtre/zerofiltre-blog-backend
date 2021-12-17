package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;

import java.util.*;
import java.util.stream.*;

@Component
@RequiredArgsConstructor
public class TagDatabaseProvider implements TagProvider {

    private final TagJPARepository repository;
    private final TagJPAMapper mapper = Mappers.getMapper(TagJPAMapper.class);


    @Override
    public Optional<Tag> tagOfId(long tagId) {
        return repository.findById(tagId)
                .map(mapper::fromJPA);
    }

    @Override
    public List<Tag> tags() {
        return repository.findAll()
                .stream().map(mapper::fromJPA)
                .collect(Collectors.toList());
    }

    @Override
    public Tag save(Tag tag) {
        return mapper.fromJPA(repository.save(mapper.toJPA(tag)));
    }
}
