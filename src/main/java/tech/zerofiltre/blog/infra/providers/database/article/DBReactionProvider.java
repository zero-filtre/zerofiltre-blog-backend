package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

import java.util.*;
import java.util.stream.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBReactionProvider implements ReactionProvider {

    private final ReactionJPARepository repository;
    private final ReactionJPAMapper mapper = Mappers.getMapper(ReactionJPAMapper.class);
    private final UserJPAMapper userMapper = Mappers.getMapper(UserJPAMapper.class);


    @Override
    public Optional<Reaction> reactionOfId(long reactionId) {
        return repository.findById(reactionId)
                .map(mapper::fromJPA);
    }

    @Override
    public List<Reaction> reactions() {
        return repository.findAll()
                .stream().map(mapper::fromJPA)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reaction> ofUser(User user) {
        return repository.findByAuthor(userMapper.toJPA(user))
                .stream().map(mapper::fromJPA)
                .collect(Collectors.toList());
    }


    @Override
    public Reaction save(Reaction reaction) {
        return mapper.fromJPA(repository.save(mapper.toJPA(reaction)));
    }

    @Override
    public void delete(Reaction reaction) {
        repository.delete(mapper.toJPA(reaction));
    }
}
