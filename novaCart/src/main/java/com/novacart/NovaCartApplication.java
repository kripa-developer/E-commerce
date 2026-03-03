package com.novacart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.novacart")
public class NovaCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaCartApplication.class, args);
    }
}
