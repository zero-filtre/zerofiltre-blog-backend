package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.UserCompanyInfoVM;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVM {

    private User user;
    private List<UserCompanyInfoVM> companies;

}
