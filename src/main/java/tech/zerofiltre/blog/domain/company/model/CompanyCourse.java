package tech.zerofiltre.blog.domain.company.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.domain.course.model.Course;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCourse extends Course {

    private boolean exclusive;

}
