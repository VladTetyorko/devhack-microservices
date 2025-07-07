package com.vladte.devhack.parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main entry point for the parser module.
 * This allows the module to be run as a separate Spring Boot application.
 */
@SpringBootApplication
@EntityScan(basePackages = {"com.vladte.devhack.entities"})
@EnableJpaRepositories
public class ParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParserApplication.class, args);
    }
}