package com.example.libraryeventproducer.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

// not recommended for production environment
@Configuration
@Profile("local")
public class AutoCreateConfig {
    // Programmatically create a new topic with Kafka admin
    @Bean
    public NewTopic libraryEvents() {
        return TopicBuilder.name("library-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
