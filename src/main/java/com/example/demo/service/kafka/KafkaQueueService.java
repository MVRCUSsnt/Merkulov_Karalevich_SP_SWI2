package com.example.demo.service.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KafkaQueueService {

    private final KafkaProperties kafkaProperties;

    public KafkaQueueService(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Читает сообщения из Kafka для конкретного пользователя (groupId = username).
     * Возвращает только сообщения главной комнаты (roomId=1).
     * Коммитит offset, чтобы повторно не отдавать те же сообщения.
     */
    public List<String> pollMainRoomMessages(String username, int maxMessages) {

        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));

        // ВАЖНО: groupId = username → отдельная очередь для каждого пользователя
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-" + username);

        // ВАЖНО: если у пользователя ещё нет offset, начинаем читать с начала
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Чтобы "не потерять" сообщения, коммитим вручную после чтения
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Явно задаём десериализаторы (на всякий)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            consumer.subscribe(Collections.singletonList("chat-messages"));

            // Небольшой poll чтобы consumer вступил в группу
            consumer.poll(Duration.ofMillis(300));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

            List<String> mainRoom = new ArrayList<>();

            records.records("chat-messages").forEach(record -> {
                String value = record.value();

                if (value != null && value.startsWith("roomId=1;")) {
                    mainRoom.add(value);
                }
            });

            // коммитим offsets: теперь эти сообщения для этого пользователя считаются "прочитанными"
            consumer.commitSync();

            return mainRoom;
        }
    }
}
