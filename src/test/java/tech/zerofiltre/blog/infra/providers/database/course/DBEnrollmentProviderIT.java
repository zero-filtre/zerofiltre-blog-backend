package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBUserProvider.class, DBCourseProvider.class, DBEnrollmentProvider.class})
class DBEnrollmentProviderIT {

    @Autowired
    private DBEnrollmentProvider dbEnrollmentProvider;
    @Autowired
    private DBUserProvider userProvider;
    @Autowired
    private DBCourseProvider courseProvider;


    @Test
    void of_returns_ProperPageOfEnrollment() throws ZerofiltreException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Course course1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course1 = courseProvider.save(course1);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course1);
        enrollment1.setUser(user);

        dbEnrollmentProvider.save(enrollment);
        dbEnrollmentProvider.save(enrollment1);

        //when
        Page<Enrollment> result = dbEnrollmentProvider.of(0, 2, user.getId(), null, null);


        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(2);
        assertThat(result.getTotalNumberOfElements()).isEqualTo(2);
        assertThat(result.getTotalNumberOfPages()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getHasNext()).isFalse();

    }

    @Test
    void of_does_not_return_inactive_enrollments() throws ZerofiltreException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Course course1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course1 = courseProvider.save(course1);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course1);
        enrollment1.setUser(user);

        dbEnrollmentProvider.save(enrollment);
        enrollment1 = dbEnrollmentProvider.save(enrollment1);
        enrollment1.setActive(false);
        dbEnrollmentProvider.save(enrollment1);

        //when
        Page<Enrollment> result = dbEnrollmentProvider.of(0, 2, user.getId(), null, null);


        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getTotalNumberOfElements()).isEqualTo(1);

    }
}
