package com.example.libraryeventproducer.producer;

import com.example.libraryeventproducer.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LibraryEventProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String TOPIC_VALUE = "library-events";

    public LibraryEventProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendLibraryEvent(LibraryEvent libraryEvent) {

        Integer key = libraryEvent.getLibraryEventId();
        String value = "";

        try {
            value = objectMapper.writeValueAsString(libraryEvent);

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        // if you use sendDefault you don't have to explicitly set the topic value
        // we will only provide the key and value
        // this is tied to default topic in application.properties/yml
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
        String finalValue = value;
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

    public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent_Approach2(LibraryEvent libraryEvent) {
        Integer key = libraryEvent.getLibraryEventId();
        String value = "";

        try {
            value = objectMapper.writeValueAsString(libraryEvent);

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }

        // headers add additional information about messages that you are publishing, can only use headers
        // with a producerRecord

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, TOPIC_VALUE);

        // send takes topic, key and value or producerRecord
        // advantage of using send is, you can publish to 'n' number of topics with the same kafkaTemplate instance
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

        String finalValue = value;

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

        return listenableFuture;
    }

    public SendResult<Integer, String> sendLibraryEventSynchronously(LibraryEvent libraryEvent) {
        Integer key = libraryEvent.getLibraryEventId();
        String value = "";
        SendResult<Integer, String> sendResult = null;

        try {
            value = objectMapper.writeValueAsString(libraryEvent);

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        // if you use sendDefault you don't have to explicitly set the topic value
        // adding .get will make this method wait until onFailure or onSuccess is resolved
        try {
            // can optionally give a timeout value for get
            // if requirement is to wait for messages to be successfully saved before responding, this is the
            // best approach
            sendResult = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Interrupted/Execution: {}", e.getMessage());
            try {
                throw e;
            } catch (Throwable ex) {
                log.error(ex.getMessage());
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            try {
                throw e;
            } catch (Throwable ex) {
                log.error(ex.getMessage());
            }
        }
        return sendResult;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {
        // recordHeader takes either (string and byte[]) or (string and byteBuffer)
        // getBytes method on String class convert the string into a byte array
        // event-source = origin of the message
        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));

        // args = topic, partition, key, value and headers
        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
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
