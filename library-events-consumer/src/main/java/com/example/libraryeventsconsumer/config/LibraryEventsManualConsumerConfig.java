//package com.example.libraryeventsconsumer.config;
//
//import com.example.libraryeventsconsumer.service.LibraryService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.dao.RecoverableDataAccessException;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.listener.ContainerProperties;
//import org.springframework.retry.RetryPolicy;
//import org.springframework.retry.backoff.FixedBackOffPolicy;
//import org.springframework.retry.policy.SimpleRetryPolicy;
//import org.springframework.retry.support.RetryTemplate;
//
//import java.util.*;
//
//@Configuration
//@EnableKafka
//@Slf4j
//public class LibraryEventsManualConsumerConfig {
//
//    private final LibraryService libraryService;
//
//    public LibraryEventsManualConsumerConfig(LibraryService libraryService) {
//        this.libraryService = libraryService;
//    }
//
//
//    @Bean
//    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer, ConsumerFactory<Object, Object> kafkaConsumerFactory) {
//        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory();
//        configurer.configure(factory, kafkaConsumerFactory);
//
//        // specifies that we will run 3 instances of this listener(3 threads), only need this if
//        // you are not running your application in a cloud environment or not using kubernetes
//        factory.setConcurrency(3);
//
//        //custom error handling
//        factory.setErrorHandler(((thrownException, data) -> {
//            log.info("Exception in consumer config: {}, record: {}", thrownException.getMessage(), data);
//        }));
//
//        //add retries
//
//        factory.setRetryTemplate(retryTemplate());
//        factory.setRecoveryCallback((retryContext -> {
//            if(retryContext.getLastThrowable().getCause() instanceof  RecoverableDataAccessException) {
//                // recovery
//                log.info("recovery");
////                Arrays.asList(retryContext.attributeNames()).forEach(attributeName -> {
////                    log.info(attributeName);
////                    log.info(Objects.requireNonNull(retryContext.getAttribute(attributeName)).toString());
////                });
//                // get consumer record
//                ConsumerRecord<Integer, String> consumerRecord = (ConsumerRecord<Integer, String>) retryContext.getAttribute("record");
//
//                libraryService.handleRecovery(consumerRecord);
//            } else {
//                log.info("Non-recoverable error");
//                throw new RuntimeException(retryContext.getLastThrowable().getMessage());
//            }
//            return null;
//        }));
//
//        // override AckMode from batch  to Manual
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//        return factory;
//    }
//
//    private RetryTemplate retryTemplate() {
//
//        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
//
//        // 1000 milliseconds
//        fixedBackOffPolicy.setBackOffPeriod(1000);
//
//        RetryTemplate retryTemplate = new RetryTemplate();
//        retryTemplate.setRetryPolicy(simpleRetryPolicy());
//        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
//
//        return retryTemplate;
//    }
//
//    private RetryPolicy simpleRetryPolicy() {
////        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
////
////        simpleRetryPolicy.setMaxAttempts(3);
//
//        Map<Class<? extends  Throwable>, Boolean> exceptionsMap = new HashMap<>();
//
//        // if false you don't want to retry, if true you want to retry
//        exceptionsMap.put(IllegalArgumentException.class, false);
//        exceptionsMap.put(RecoverableDataAccessException.class, true);
//
//        // num of retries map of exceptions that you want to retry on, traverseCause
//        return new SimpleRetryPolicy(3, exceptionsMap, true);
//    }
//}
//
//
//
