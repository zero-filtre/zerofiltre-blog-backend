package tech.zerofiltre.blog;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.retry.annotation.*;
import org.springframework.scheduling.annotation.*;

@EnableScheduling
@EnableRetry
@SpringBootApplication
public class ZerofiltreBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZerofiltreBlogApplication.class, args);
    }


}
