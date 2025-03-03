package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResultJPA {

    long id;
    String fullName;
    String profilePicture;
}
