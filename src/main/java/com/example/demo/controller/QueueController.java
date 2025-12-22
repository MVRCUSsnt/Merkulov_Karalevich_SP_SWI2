package com.example.demo.controller;

import com.example.demo.service.kafka.KafkaQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final KafkaQueueService kafkaQueueService;

    public QueueController(KafkaQueueService kafkaQueueService) {
        this.kafkaQueueService = kafkaQueueService;
    }

    @GetMapping("/main")
    public ResponseEntity<?> pollMainQueue(Principal principal,
                                           @RequestParam(defaultValue = "50") int limit) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            List<String> messages = kafkaQueueService.pollMainRoomMessages(principal.getName(), limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            // НЕ ЛОМАЕМ демо, если Kafka временно недоступна
            return ResponseEntity.ok(List.of());
        }
    }
}
