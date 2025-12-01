package tech.zerofiltre.blog.infra.providers.database.company.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCourseJPA extends CourseJPA {

    private boolean exclusive;

    public CompanyCourseJPA(CourseJPA courseJPA, boolean exclusive) {
        super(courseJPA);
        this.exclusive = exclusive;
    }
}
