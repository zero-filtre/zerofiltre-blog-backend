package tech.zerofiltre.blog.infra.providers.database.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserEmail;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import java.util.List;
import java.util.Optional;

public interface UserJPARepository extends JpaRepository<UserJPA, Long> {
    Optional<UserJPA> findByEmail(String email);

    Optional<UserJPA> findByPaymentEmail(String email);


    List<UserJPA> findByIsActiveIsFalse();

    Optional<UserJPA> findBySocialId(String userSocialId);

    @Query("select u.id, u.fullName, u.profilePicture from UserJPA u " +
            "join CourseJPA co on co.author.id=u.id " +
            "where co.id=:courseId")
    String findAuthorInfoByCourseId(long courseId);

    @Query("select new tech.zerofiltre.blog.infra.providers.database.user.model.UserEmail(email, paymentEmail) from UserJPA")
    List<UserEmail> findAllEmails();
}
