package com.example.libraryeventsconsumer.consumer;

import com.example.libraryeventsconsumer.service.LibraryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
public class LibraryEventConsumer {

    private final LibraryService libraryService;

    public LibraryEventConsumer(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // listeners for kafka messages
    // uses ConcurrentKafkaListenerContainerFactory behind the scenes
    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {

        try {
            log.info("Processing message");
            libraryService.processLibraryEvent(consumerRecord);
            log.info("Processing has ended...");
        } catch(Exception e) {
            log.error("error processing message: {}", e.getMessage());
        }
    }

}
