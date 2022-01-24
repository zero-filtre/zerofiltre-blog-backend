package tech.zerofiltre.blog.infra.providers.database.user.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

@Mapper
public interface VerificationTokenJPAMapper {

    VerificationToken fromJPA(VerificationTokenJPA verificationTokenJPA);

    VerificationTokenJPA toJPA(VerificationToken verificationToken);
}
