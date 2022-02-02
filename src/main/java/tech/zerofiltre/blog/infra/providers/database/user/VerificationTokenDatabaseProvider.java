package tech.zerofiltre.blog.infra.providers.database.user;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class VerificationTokenDatabaseProvider implements VerificationTokenProvider {

    private final VerificationTokenJPARepository repository;
    private final VerificationTokenJPAMapper mapper = Mappers.getMapper(VerificationTokenJPAMapper.class);
    private final UserJPAMapper userJPAMapper = Mappers.getMapper(UserJPAMapper.class);

    @Override
    public Optional<VerificationToken> ofToken(String token) {
        return repository.findByToken(token)
                .map(mapper::fromJPA);
    }

    @Override
    public Optional<VerificationToken> ofUser(User user) {
        return repository.findByUser(userJPAMapper.toJPA(user))
                .map(mapper::fromJPA);
    }

    @Override
    public VerificationToken save(VerificationToken verificationToken) {
        return mapper.fromJPA(repository.save(mapper.toJPA(verificationToken)));
    }

    @Override
    public void delete(VerificationToken token) {
        repository.delete(mapper.toJPA(token));
    }
}
