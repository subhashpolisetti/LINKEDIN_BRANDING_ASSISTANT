package com.linkedinjobassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoRepositories
@EnableCaching
@EnableScheduling
public class LinkedInJobAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(LinkedInJobAssistantApplication.class, args);
    }
}
