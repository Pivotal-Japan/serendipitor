package io.pivotal.jp.serendipitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SerendipitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SerendipitorApplication.class, args);
    }

}

