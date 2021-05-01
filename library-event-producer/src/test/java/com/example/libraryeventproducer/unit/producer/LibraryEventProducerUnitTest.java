package com.example.libraryeventproducer.unit.producer;

import com.example.libraryeventproducer.domain.Book;
import com.example.libraryeventproducer.domain.LibraryEvent;
import com.example.libraryeventproducer.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import scala.Int;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

// we need mockito to mock the send call
@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    // create instance of object mapper, spy is to watch(not to mock)
    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    // mockito, for injecting mocks of class under test
    @InjectMocks
    LibraryEventProducer eventProducer;


    // test onFailure method
    @Test
    void sendLibraryEvent_Approach2_failure() {
        Book book = Book.builder().bookId(1).bookName("Moby Dick").bookAuthor("John Doe").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();

        SettableListenableFuture future = new SettableListenableFuture();

        future.setException(new RuntimeException("Exception"));

        // mock send call
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        assertThrows(Exception.class, () -> eventProducer.sendLibraryEvent_Approach2(libraryEvent).get());
    }

    // test onSuccess method
    @Test
    void sendLibraryEvent_Approach2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        Book book = Book.builder().bookId(1).bookName("Moby Dick").bookAuthor("John Doe").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();

        SettableListenableFuture future = new SettableListenableFuture();

        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("library-events",
                libraryEvent.getLibraryEventId(), objectMapper.writeValueAsString(libraryEvent));

        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1),
                1, 1, 342, System.currentTimeMillis(), 1, 2);

        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.set(sendResult);

        // mock send call
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = eventProducer.sendLibraryEvent_Approach2(libraryEvent);

        // get end result
        SendResult<Integer, String> finalSendResult = listenableFuture.get();

        assert finalSendResult.getRecordMetadata().partition() == 1;
    }
}
