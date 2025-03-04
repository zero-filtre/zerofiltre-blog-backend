package tech.zerofiltre.blog.domain.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResult implements Serializable {

        long id;
        String fullName;
        String profilePicture;
}
