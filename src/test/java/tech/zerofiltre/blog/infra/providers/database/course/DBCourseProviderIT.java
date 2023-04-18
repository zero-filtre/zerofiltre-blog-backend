package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
class DBCourseProviderIT {

    DBCourseProvider courseProvider;


    DBUserProvider userProvider;

    @Autowired
    CourseJPARepository courseJPARepository;


    @Autowired
    UserJPARepository userJPARepository;

    @BeforeEach
    void init() {
        courseProvider = new DBCourseProvider(courseJPARepository);
        userProvider = new DBUserProvider(userJPARepository);
    }

    @Test
    void savingACourse_isOK() {
        User author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(),Collections.emptyList());
        course = courseProvider.save(course);

        assertThat(course.getId()).isNotZero();
    }

    @Test
    void getACourseByItsId_isOk() {
        User author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(),Collections.emptyList());
        course = courseProvider.save(course);

        Optional<Course> courseOptional = courseProvider.courseOfId(course.getId());

        assertThat(courseOptional).isPresent();
        assertThat(courseOptional.get().getId()).isEqualTo(course.getId());
    }
}
