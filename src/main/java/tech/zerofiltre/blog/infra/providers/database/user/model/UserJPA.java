package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import javax.persistence.*;
import java.time.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserJPA extends BaseEntity {

    private String pseudoName;
    private String firstName;
    private String lastName;
    private LocalDateTime registeredOn;
    private String profilePicture;

}
