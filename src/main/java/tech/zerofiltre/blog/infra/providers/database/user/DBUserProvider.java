package tech.zerofiltre.blog.infra.providers.database.user;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class DBUserProvider implements UserProvider {
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
    //@Cacheable(value = "connected-user", key = "#email") - Buggy
    public Optional<User> userOfEmail(String email) {
        Optional<UserJPA> result = repository.findByEmail(email);
        if (result.isEmpty()) result = repository.findByPaymentEmail(email);
        return result.map(mapper::fromJPA);
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
        userJPA.setSocialLinks(null);
        repository.delete(userJPA);
    }

    @Override
    public Optional<User> userOfSocialId(String userSocialId) {
        return repository.findBySocialId(userSocialId)
                .map(mapper::fromJPA);
    }

    @Override
    public List<UserForBroadcast> allUsersForBroadcast() {
        return repository.findAllUsersForBroadcast();
    }
}
