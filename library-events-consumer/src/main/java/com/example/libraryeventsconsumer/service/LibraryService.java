package com.example.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface LibraryService {
    void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException;
}
