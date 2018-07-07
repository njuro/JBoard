package com.github.njuro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EntityScan(
        basePackageClasses = {JBoardApplication.class, Jsr310JpaConverters.class}
)

@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
public class JBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(JBoardApplication.class, args);
    }
}
