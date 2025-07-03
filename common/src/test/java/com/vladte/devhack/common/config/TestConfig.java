package com.vladte.devhack.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Test configuration class for unit tests.
 * Enables AspectJ auto-proxy and component scanning for test-specific components.
 * Configures an in-memory H2 database for testing.
 */
@TestConfiguration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EntityScan(basePackages = {"com.vladte.devhack.entities"})
@EnableJpaRepositories(basePackages = {"com.vladte.devhack.common.repository"})
@ComponentScan(basePackages = {"com.vladte.devhack.common.aspect", "com.vladte.devhack.common.repository"})
public class TestConfig {

    /**
     * Creates an embedded H2 database for testing.
     * This is used when the application-test.properties configuration is not sufficient.
     *
     * @return the data source
     */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }
}
