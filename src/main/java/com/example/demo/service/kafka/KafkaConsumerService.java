package com.example.demo.service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @KafkaListener(topics = "chat-messages", groupId = "my-group")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.println("Received message: " + record.value());
    }
}
