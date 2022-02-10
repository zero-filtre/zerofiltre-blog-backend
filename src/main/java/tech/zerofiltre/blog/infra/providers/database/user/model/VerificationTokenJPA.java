package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;
import java.time.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class VerificationTokenJPA extends BaseEntityJPA {

    private String token;

    @OneToOne
    @JoinColumn(nullable = false, name = "user_id")
    private UserJPA user;

    private LocalDateTime expiryDate;

}
