package com.novacart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication(scanBasePackages = "com.novacart")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class NovaCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaCartApplication.class, args);
    }
}
