package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
        course.setMentored(true);
        course = courseProvider.save(course);

        assertThat(course.getId()).isNotZero();
        assertThat(course.isMentored()).isTrue();
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
        assertThat(courseOptional.get().isMentored()).isFalse();
    }
}
