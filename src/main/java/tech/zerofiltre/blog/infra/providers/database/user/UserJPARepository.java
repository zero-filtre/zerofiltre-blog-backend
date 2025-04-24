package tech.zerofiltre.blog.infra.providers.database.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserSearchResultJPA;

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

    @Query("select new tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast" +
            "(u.id, u.email, u.paymentEmail, u.language, u.fullName) from UserJPA u " +
            "where u.subscribedToBroadcast = true")
    List<UserForBroadcast> findAllUsersForBroadcast();

    @Query("select new tech.zerofiltre.blog.infra.providers.database.user.model.UserSearchResultJPA" +
            "(user.id,user.fullName,user.profilePicture) from UserJPA user " +
            "where (LOWER(user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(user.pseudoName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(user.paymentEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(user.profession) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(user.bio) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    List<UserSearchResultJPA> findByKeyword(String keyword);

}
