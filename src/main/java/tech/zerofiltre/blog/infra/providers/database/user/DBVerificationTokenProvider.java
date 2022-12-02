package tech.zerofiltre.blog.infra.providers.database.user;

import org.mapstruct.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

import java.time.*;
import java.util.*;

@Component
@Transactional
public class DBVerificationTokenProvider implements VerificationTokenProvider {

    private final VerificationTokenJPARepository repository;
    private final VerificationTokenJPAMapper mapper = Mappers.getMapper(VerificationTokenJPAMapper.class);
    private final UserJPAMapper userJPAMapper = Mappers.getMapper(UserJPAMapper.class);

    @Value("${zerofiltre.infra.security.verification-token.expiration-seconds:604800}")
    private long expiration;

    public DBVerificationTokenProvider(VerificationTokenJPARepository repository) {
        this.repository = repository;
    }

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
    public VerificationToken generate(User user, long durationInSeconds) {
        return getVerificationToken(user, durationInSeconds);
    }


    @Override
    public void delete(VerificationToken token) {
        repository.delete(mapper.toJPA(token));
    }


    @Override
    /*
      Generates token with the configured expiration
     */
    public VerificationToken generate(User user) {
        return getVerificationToken(user, expiration);
    }

    private VerificationToken getVerificationToken(User user, long durationInSeconds) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(durationInSeconds);
        VerificationToken verificationToken = ofUser(user).map(vToken -> {
            vToken.setToken(token);
            vToken.setExpiryDate(expiryDate);
            return vToken;
        }).orElse(new VerificationToken(user, token, expiryDate));
        return save(verificationToken);
    }


}
