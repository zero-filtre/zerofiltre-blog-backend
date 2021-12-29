package tech.zerofiltre.blog;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.*;
import org.springframework.core.env.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.util.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@SpringBootApplication
public class ZerofiltreBlogApplication {
    static ConfigurableApplicationContext applicationContext;
    static Environment environment;
    static TagProvider tagProvider;
    static UserProvider userProvider;
    static ReactionProvider reactionProvider;
    static ArticleProvider articleProvider;

    public static void main(String[] args) {

        applicationContext = SpringApplication.run(ZerofiltreBlogApplication.class, args);
        environment = applicationContext.getEnvironment();
        tagProvider = applicationContext.getBean(TagProvider.class);
        userProvider = applicationContext.getBean(UserProvider.class);
        reactionProvider = applicationContext.getBean(ReactionProvider.class);
        articleProvider = applicationContext.getBean(ArticleProvider.class);


        initDB();
    }

    private static void initDB() {
        if (!environment.acceptsProfiles(Profiles.of("prod","dev","uat"))) {
            User mockUser = userProvider.create(ZerofiltreUtils.createMockUser());

            List<Tag> mockTags = ZerofiltreUtils.createMockTags(false).stream()
                    .map(tagProvider::create)
                    .collect(Collectors.toList());


            for (long i = 1; i <= 20; i++) {
                Article toSave = ZerofiltreUtils.createMockArticle(mockUser, mockTags, Collections.emptyList());
                toSave.setId(0);
                toSave.setTitle("N°" + i + ": " + toSave.getTitle());
                toSave.setPublishedAt(LocalDateTime.now().minusDays(i));
                articleProvider.save(toSave);
            }
        }
    }


}
