package com.example.libraryeventproducer.controller;

import com.example.libraryeventproducer.domain.LibraryEvent;
import com.example.libraryeventproducer.domain.LibraryEventType;
import com.example.libraryeventproducer.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/library")
@Slf4j
public class LibraryController {

    private final LibraryEventProducer libraryEventProducer;

    public LibraryController(LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }


    @PostMapping("/event")
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryEvent postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) {

        libraryEvent.setLibraryEventType(LibraryEventType.NEW);

        // invoke kafka producer with asynchronous method call
        // libraryEventProducer.sendLibraryEvent(libraryEvent);
        // invoke kafka producer with synchronous method call
        // SendResult<Integer, String> sendResult = libraryEventProducer.sendLibraryEventSynchronously(libraryEvent);
        libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent);

        // will print the producerRecord and the recordMetaData
        // log.info("Send Result: {}", sendResult.toString());
        return libraryEvent;
    }

    @PutMapping("/event")
    @ResponseStatus(HttpStatus.OK)
    public LibraryEvent putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) {

        if(libraryEvent.getLibraryEventId() == null) {
            throw new RuntimeException();
        }

        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);

        libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent);

        return libraryEvent;
    }
}
