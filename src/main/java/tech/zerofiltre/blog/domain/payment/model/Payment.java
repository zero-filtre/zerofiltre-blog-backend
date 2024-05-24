package tech.zerofiltre.blog.domain.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private User user;
    private String reference;
    private long id;
    private LocalDateTime at = LocalDateTime.now();

}
