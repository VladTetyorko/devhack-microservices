package com.vladte.devhack.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.vladte.devhack")
@EnableAsync
@EnableKafka
@Import(com.vladte.devhack.domain.config.DomainAutoConfiguration.class)
public class DevHackApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevHackApplication.class, args);
    }

}
