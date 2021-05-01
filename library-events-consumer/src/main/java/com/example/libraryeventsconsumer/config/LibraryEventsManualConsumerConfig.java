//package com.example.libraryeventsconsumer.config;
//
//import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.listener.ContainerProperties;
//
//@Configuration
//@EnableKafka
//public class LibraryEventsManualConsumerConfig {
//    @Bean
//    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer, ConsumerFactory<Object, Object> kafkaConsumerFactory) {
//        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory();
//        configurer.configure(factory, kafkaConsumerFactory);
//
//        // specifies that we will run 3 instances of this listener(3 threads), only need this if
//        // you are not running your application in a cloud environment or not using kubernetes
//        factory.setConcurrency(3);
//
//        // override AckMode from batch  to Manual
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//        return factory;
//    }
//}
