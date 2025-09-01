package com.vladte.devhack.domain.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan("com.vladte.devhack.domain")
@EnableJpaRepositories("com.vladte.devhack.domain.repository")
@EntityScan("com.vladte.devhack.domain.entities")
public class DomainAutoConfiguration {
}
