package tech.zerofiltre.blog.infra.providers.database.user;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

import java.util.*;
import java.util.stream.*;

@Component
@RequiredArgsConstructor
public class UserDatabaseProvider implements UserProvider {
    private final UserJPARepository repository;
    private final UserJPAMapper mapper = Mappers.getMapper(UserJPAMapper.class);

    @Override
    public Optional<User> userOfId(long userId) {
        return repository.findById(userId)
                .map(mapper::fromJPA);
    }

    @Override
    public List<User> users() {
        return repository.findAll()
                .stream().map(mapper::fromJPA)
                .collect(Collectors.toList());
    }

    @Override
    public User create(User user) {
        return mapper.fromJPA(repository.save(mapper.toJPA(user)));
    }
}
