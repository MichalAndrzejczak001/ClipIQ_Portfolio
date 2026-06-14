package com.clipiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ClipIQApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClipIQApplication.class, args);
    }
}
