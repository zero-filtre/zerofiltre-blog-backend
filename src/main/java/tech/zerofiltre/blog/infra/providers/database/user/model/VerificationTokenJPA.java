package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;

import javax.persistence.*;
import java.time.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "user")
@Table(name = "verification_token")
public class VerificationTokenJPA extends BaseEntityJPA {

    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private UserJPA user;

    private LocalDateTime expiryDate;

}
