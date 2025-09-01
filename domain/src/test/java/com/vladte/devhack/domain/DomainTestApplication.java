package com.vladte.devhack.domain;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Minimal Spring Boot test configuration for the domain module.
 * Provides a discoverable @SpringBootConfiguration so Spring test bootstrapper
 * can start a context for @DataJpaTest and other tests.
 */
@SpringBootConfiguration
@EntityScan("com.vladte.devhack.domain.entities")
@EnableJpaRepositories(basePackages = "com.vladte.devhack.domain.repository")
public class DomainTestApplication {
}
