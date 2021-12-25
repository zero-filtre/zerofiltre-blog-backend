package tech.zerofiltre.blog;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.*;
import org.springframework.core.env.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.util.*;

@SpringBootApplication
public class ZerofiltreBlogApplication {
    static ConfigurableApplicationContext applicationContext;
    static Environment environment;
    static TagProvider tagProvider;
    static UserProvider userProvider;

    public static void main(String[] args) {

        applicationContext = SpringApplication.run(ZerofiltreBlogApplication.class, args);
        environment = applicationContext.getEnvironment();

        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            tagProvider = applicationContext.getBean(TagProvider.class);
            UserProvider userProvider = applicationContext.getBean(UserProvider.class);
            userProvider.create(ZerofiltreUtils.createMockUser());
            ZerofiltreUtils.createMockTags(true).forEach(tagProvider::create);
        }
    }


}
