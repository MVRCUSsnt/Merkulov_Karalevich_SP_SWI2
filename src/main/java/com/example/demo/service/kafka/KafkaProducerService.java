package com.example.demo.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println(
                                "Kafka ERROR → topic=" + topic +
                                        ", reason=" + ex.getMessage()
                        );
                    } else if (result != null) {
                        System.out.println(
                                "Kafka SUCCESS → topic=" + topic +
                                        ", partition=" + result.getRecordMetadata().partition() +
                                        ", offset=" + result.getRecordMetadata().offset()
                        );
                    }
                });
    }


}
