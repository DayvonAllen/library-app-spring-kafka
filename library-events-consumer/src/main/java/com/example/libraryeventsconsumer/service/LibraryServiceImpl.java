package com.example.libraryeventsconsumer.service;

import com.example.libraryeventsconsumer.model.LibraryEvent;
import com.example.libraryeventsconsumer.repo.BookRepo;
import com.example.libraryeventsconsumer.repo.LibraryEventRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
@Transactional
@Slf4j
public class LibraryServiceImpl implements LibraryService {

    private final LibraryEventRepo libraryEventRepo;
    private final BookRepo bookRepo;
    private final ObjectMapper objectMapper;

    public LibraryServiceImpl(LibraryEventRepo libraryEventRepo, BookRepo bookRepo, ObjectMapper objectMapper) {
        this.libraryEventRepo = libraryEventRepo;
        this.bookRepo = bookRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        // unmarshal message json value into Java object 'LibraryEvent'
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                libraryEvent.getBook().setLibraryEvent(libraryEvent);
                bookRepo.save(libraryEvent.getBook());
                log.info("Process message and saved to the database : {}", libraryEvent.toString());
                break;
            case UPDATE:
                libraryEvent.getBook().setLibraryEvent(libraryEvent);
                validate(libraryEvent);
                libraryEventRepo.save(libraryEvent);
                log.info("Process message and updated to the database : {}", libraryEvent.toString());
                break;
            default:
                log.info("invalid library event type");
        }
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
}
