package com.example.libraryeventsconsumer.service;

import com.example.libraryeventsconsumer.model.LibraryEvent;
import com.example.libraryeventsconsumer.repo.BookRepo;
import com.example.libraryeventsconsumer.repo.LibraryEventRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.transaction.Transactional;


@Service
@Transactional
@Slf4j
public class LibraryServiceImpl implements LibraryService {

    private final LibraryEventRepo libraryEventRepo;
    private final BookRepo bookRepo;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Integer, String> kafkaTemplate;

    public LibraryServiceImpl(LibraryEventRepo libraryEventRepo, BookRepo bookRepo, ObjectMapper objectMapper, KafkaTemplate<Integer, String> kafkaTemplate) {
        this.libraryEventRepo = libraryEventRepo;
        this.bookRepo = bookRepo;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        // unmarshal message json value into Java object 'LibraryEvent'
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                libraryEvent.getBook().setLibraryEvent(libraryEvent);
                bookRepo.save(libraryEvent.getBook());
                log.info("Process message and saved to the database : {}", libraryEvent);
                break;
            case UPDATE:
                libraryEvent.getBook().setLibraryEvent(libraryEvent);
                validate(libraryEvent);
                libraryEventRepo.save(libraryEvent);
                 log.info("Process message and updated to the database : {}", libraryEvent);
                break;
            default:
                log.info("invalid library event type");
        }
    }

    @Override
    public void handleRecovery(ConsumerRecord<Integer, String> consumerRecord) {
        Integer key = consumerRecord.key();
        String message = consumerRecord.value();
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
        String finalValue = message;
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, finalValue, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, finalValue, result);
            }
        });
    }

    private void validate(LibraryEvent libraryEvent) {
        System.out.println(libraryEvent.getLibraryEventId());
        // library event ID should never be null for an update event type
        if(libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("library event Id is missing");
        }

        LibraryEvent libEvent = libraryEventRepo.findById(libraryEvent.getLibraryEventId()).orElseThrow(() -> new IllegalArgumentException("ID: " +  libraryEvent.getLibraryEventId() + " is not found"));
        log.info("Passed!");
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message Sent Successfully for the key : {} and the value is {}, partition is {}", key, value, result.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Message Sent Unsuccessfully for the key : {} and the value is {}", key, value);
        try {
            throw ex;
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }
}
