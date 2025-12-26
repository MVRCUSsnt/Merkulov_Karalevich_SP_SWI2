package com.example.demo.controller;

import com.example.demo.Message;
import com.example.demo.Room;
import com.example.demo.Users;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.PrivateMessageDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.kafka.KafkaProducerService;

import java.security.Principal;
import java.time.LocalDateTime; 
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public MessageController(MessageRepository messageRepository,
                             MessageService messageService,
                             SimpMessagingTemplate messagingTemplate,
                             UserRepository userRepository, KafkaProducerService kafkaProducerService) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    // --- УДАЛЕНИЕ ---
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id, Principal principal) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        Long roomId = message.getRoom().getId();

        messageService.deleteMessage(id, principal);

        // TODO: Раскомментировать, когда Фронтенд научится понимать события "DELETE"
        /*
        Map<String, Object> deleteNotification = new HashMap<>();
        deleteNotification.put("type", "DELETE");
        deleteNotification.put("messageId", id);
        messagingTemplate.convertAndSend("/topic/messages/" + roomId, deleteNotification);
        */

        return ResponseEntity.ok("Message deleted successfully");
    }

    // --- РЕДАКТИРОВАНИЕ ---
    @PutMapping("/edit/{id}")
    public ResponseEntity<MessageDTO> editMessage(@PathVariable Long id,
                                                  @RequestBody MessageDTO messageDTO,
                                                  Principal principal) {
        Message updatedMessage = messageService.updateMessage(id, messageDTO);
        MessageDTO responseDTO = convertToDTO(updatedMessage);

        // TODO: Раскомментировать, когда Фронтенд научится обновлять старые сообщения
        /*
        messagingTemplate.convertAndSend("/topic/messages/" + updatedMessage.getRoom().getId(), responseDTO);
        */

        return ResponseEntity.ok(responseDTO);
    }

    // --- ФИЛЬТРАЦИЯ ---
    @GetMapping
    public ResponseEntity<List<PrivateMessageDTO>> getFilteredMessages(@RequestParam(required = false) String filter) {
        List<PrivateMessageDTO> messages = messageService.getFilteredMessages(filter);
        return ResponseEntity.ok(messages);
    }

    // --- ПОЛУЧЕНИЕ СООБЩЕНИЙ ЧАТА ---
    @GetMapping("/{roomId}")
    public List<MessageDTO> getMessages(@PathVariable Long roomId) {
        List<Message> messages = messageRepository.findByRoomId(roomId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- ОТПРАВКА СООБЩЕНИЯ (ИСПРАВЛЕНО ВРЕМЯ И БЕЗОПАСНОСТЬ) ---
    @PostMapping("/write")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO, Principal principal) {
        Message message = new Message();
        message.setContent(messageDTO.getContent());

        // ✅ ИСПРАВЛЕНИЕ: Используем LocalDateTime вместо Date
        message.setTimestamp(LocalDateTime.now());

        Room room = new Room();
        room.setId(messageDTO.getRoomId());
        message.setRoom(room);

        // Безопасное получение пользователя
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        message.setUsers(user);

        messageRepository.save(message);

        try {
            String kafkaPayload = String.format(
                    "roomId=%d; user=%s; content=%s",
                    message.getRoom().getId(),
                    user.getUsername(),
                    message.getContent()
            );

            kafkaProducerService.sendMessage("chat-messages", kafkaPayload);
        } catch (Exception e) {
            // ВАЖНО: не ломаем чат, если Kafka недоступна
            System.err.println("Kafka send failed: " + e.getMessage());
        }

        MessageDTO responseMessage = convertToDTO(message);

        messagingTemplate.convertAndSend("/topic/messages/" + message.getRoom().getId(), responseMessage);

        return ResponseEntity.ok("Сообщение успешно сохранено и отправлено через WebSocket");
    }

    // Вспомогательный метод
    private MessageDTO convertToDTO(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getRoom().getId(),
                message.getContent(),
                message.getTimestamp().toString(), // toString() для LocalDateTime работает корректно
                message.getUsers().getId(),
                new UserDTO(
                        message.getUsers().getId(),
                        message.getUsers().getUsername(),
                        message.getUsers().getAvatarUrl()
                )
        );
    }
}