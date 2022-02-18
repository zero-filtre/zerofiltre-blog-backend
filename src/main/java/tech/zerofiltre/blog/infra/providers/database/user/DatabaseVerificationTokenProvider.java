package tech.zerofiltre.blog.infra.providers.database.user;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

import java.time.*;
import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DatabaseVerificationTokenProvider implements VerificationTokenProvider {

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
    public VerificationToken update(VerificationToken verificationToken) {
        return mapper.fromJPA(repository.save(mapper.toJPA(verificationToken)));
    }

    @Override
    public VerificationToken generate(User user, long durationInSeconds) {
        return getVerificationToken(user, durationInSeconds);
    }

    @Override
    public VerificationToken generate(User user) {
        return getVerificationToken(user, 0);
    }

    @Override
    public void delete(VerificationToken token) {
        repository.delete(mapper.toJPA(token));
    }


    private VerificationToken getVerificationToken(User user, long durationInSeconds) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = ofUser(user)
                .map(vToken -> {
                    vToken.setToken(token);
                    if (durationInSeconds != 0)
                        vToken.setExpiryDate(LocalDateTime.now().plusSeconds(durationInSeconds));
                    else
                        vToken.setExpiryDate(LocalDateTime.now().plusDays(1));
                    return vToken;
                }).orElse(new VerificationToken(user, token));

        return update(verificationToken);
    }


}
