package com.linkedinjobassistant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.linkedinjobassistant.repository")
public class MongoConfig {
    // MongoDB configuration is handled through application.yml
    // This class enables auditing and repository scanning
}
