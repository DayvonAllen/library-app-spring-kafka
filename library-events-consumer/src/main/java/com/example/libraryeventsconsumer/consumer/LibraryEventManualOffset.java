//package com.example.libraryeventsconsumer.consumer;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.listener.AcknowledgingMessageListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class LibraryEventManualOffset implements AcknowledgingMessageListener<Integer, String> {
//
//
//    @Override
//    @KafkaListener(topics = {"library-events"})
//    public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
//        // we must manually acknowledge that will processed this message
//        // the message listener is waiting for this acknowledgment and after we have acknowledged
//        // that we processed the message then the offset will be committed
//        acknowledgment.acknowledge();
//        System.out.println("We manually acknowledged that we process this message: ");
//        log.info(consumerRecord.toString());
//    }
//
//}
