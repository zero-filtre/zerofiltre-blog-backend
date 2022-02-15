package tech.zerofiltre.blog.infra.providers.database.user;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.util.*;
import java.util.stream.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DatabaseUserProvider implements UserProvider {
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
    public User save(User user) {
        return mapper.fromJPA(repository.save(mapper.toJPA(user)));
    }

    @Override
    public Optional<User> userOfEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::fromJPA);
    }

    @Override
    public List<User> nonActiveUsers() {
        return repository.findByIsActiveIsFalse()
                .stream().map(mapper::fromJPA)
                .collect(Collectors.toList());

    }

    @Override
    public void deleteUser(User user) {
        UserJPA userJPA = mapper.toJPA(user);
        userJPA.setVerificationTokenJPA(null);
        repository.delete(userJPA);
    }
}
