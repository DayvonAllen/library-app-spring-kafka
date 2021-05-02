package com.example.libraryeventsconsumer.intg.consumer;

import com.example.libraryeventsconsumer.consumer.LibraryEventConsumer;
import com.example.libraryeventsconsumer.model.LibraryEvent;
import com.example.libraryeventsconsumer.model.LibraryEventType;
import com.example.libraryeventsconsumer.repo.LibraryEventRepo;
import com.example.libraryeventsconsumer.service.LibraryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
// overriding our application.properties kafka configuration, so we can connect to the embeddedKafka instance(in memory kafka broker)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(topics = {"library-events"}, partitions = 1)
public class LibraryEventConsumerIntegrationTest {

    // all are automatically created for us by spring
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;
    // contains all the listener containers(has access to all the listener containers)
    // in our app one listener container is 'LibraryEventConsumer.java'
    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    LibraryEventRepo libraryEventRepo;

    // 'SpyBean' gives you access to the real bean. You can spy on the execution of that bean.
    @SpyBean
    LibraryEventConsumer libraryEventConsumerSpy;

    @SpyBean
    LibraryService libraryServiceSpy;

    @BeforeEach
    void setUp() {
        // get all listener containers(in our case it's just one)
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            // make sure consumer is up and running before we launch the test case
            // it's going to make our listener container wait until all partitions are assigned to it.
            // needed consumer integration tests
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        libraryEventRepo.deleteAll();
    }

    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        String json = "{\"libraryEventId\":null,\"book\":{\"bookId\":1,\"bookName\":\"Moby Dick\",\"bookAuthor\":\"John Doe\"},\"libraryEventType\":\"NEW\"}";

        // publish event synchronously
        kafkaTemplate.sendDefault(json).get();

        //when
        // 'CountDownLatch' helps us block the current execution of a thread
        // really useful when writing test cases that involves asynchronous code execution
        // as soon as the count goes from 1 it will release the thread
        CountDownLatch latch = new CountDownLatch(1);

        // Thread will be blocked for 3 seconds and then released
        // will decrement latch count after 3 seconds
        latch.await(3, TimeUnit.SECONDS);

        //then
        // verify comes from mockito
        // will verify how many times this bean is called
        verify(libraryEventConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> libraryEvents = libraryEventRepo.findAll();
        assert libraryEvents.size() == 1;
        libraryEvents.forEach(libraryEvent -> {
            assert libraryEvent.getLibraryEventId() != null;
            assertEquals(1, libraryEvent.getBook().getBookId());
        });
    }
    

    @Test
    void publishUpdateLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        setUpUpdateData();

        //given
        String update = "{\"libraryEventId\":1,\"book\":{\"bookId\":2,\"bookName\":\"Moby Dick\",\"bookAuthor\":\"John\"},\"libraryEventType\":\"UPDATE\"}";

        // publish event synchronously
        kafkaTemplate.sendDefault(update).get();


        // as soon as the count goes from 1 it will release the thread
        CountDownLatch latch = new CountDownLatch(1);

        // Thread will be blocked for 1 seconds and then released
        // will decrement latch count after 1 seconds
        latch.await(1, TimeUnit.SECONDS);

        //then
        // verify comes from mockito
        // will verify how many times this bean is called
        verify(libraryEventConsumerSpy, times(2)).onMessage(isA(ConsumerRecord.class));
        verify(libraryServiceSpy, times(2)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> libraryEvents = libraryEventRepo.findAll();
        libraryEvents.forEach(libraryEvent -> {
            if(libraryEvent.getLibraryEventId() == 1){
                assert libraryEvent.getLibraryEventType() == LibraryEventType.UPDATE;
            }
        });
        
    }

    void setUpUpdateData() throws ExecutionException, InterruptedException {
        //given
        String json = "{\"libraryEventId\":null,\"book\":{\"bookId\":1,\"bookName\":\"Moby Dick\",\"bookAuthor\":\"John Doe\"},\"libraryEventType\":\"NEW\"}";

        // publish event synchronously
        kafkaTemplate.sendDefault(json).get();

        // as soon as the count goes from 1 it will release the thread
        CountDownLatch latch = new CountDownLatch(1);

        // Thread will be blocked for 3 seconds and then released
        // will decrement latch count after 3 seconds
        latch.await(1, TimeUnit.SECONDS);
    }

}
