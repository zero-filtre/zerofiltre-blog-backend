package tech.zerofiltre.blog;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.*;
import org.springframework.data.redis.repository.configuration.*;
import org.springframework.retry.annotation.*;
import org.springframework.scheduling.annotation.*;

@EnableScheduling
@EnableRetry
@SpringBootApplication
@EnableCaching
public class ZerofiltreBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZerofiltreBlogApplication.class, args);
    }


}
