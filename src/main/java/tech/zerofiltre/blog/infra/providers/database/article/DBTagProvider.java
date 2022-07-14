package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;

import java.util.*;
import java.util.stream.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBTagProvider implements TagProvider {

    private final TagJPARepository repository;
    private final TagJPAMapper mapper = Mappers.getMapper(TagJPAMapper.class);


    @Override
    public Optional<Tag> tagOfId(long id) {
        return repository.findById(id)
                .map(mapper::fromJPA);
    }

    @Override
    public Optional<Tag> tagOfName(String name) {
        return repository.findByName(name)
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
