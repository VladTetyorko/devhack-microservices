package com.vladte.devhack.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.vladte.devhack")
@EntityScan("com.vladte.devhack.entities")
@EnableAsync
@EnableKafka
public class DevHackApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevHackApplication.class, args);
    }

}
