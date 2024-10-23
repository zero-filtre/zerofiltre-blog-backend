package tech.zerofiltre.blog.domain.company.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkCompanyCourse {

    private long id;
    private long companyId;
    private long courseId;
    private boolean active = true;
    private LocalDateTime linkedAt;
    private LocalDateTime suspendedAt;

}
