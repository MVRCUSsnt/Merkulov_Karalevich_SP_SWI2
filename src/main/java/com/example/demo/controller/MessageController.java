package com.example.demo.controller;

import com.example.demo.HiddenMessage;
import com.example.demo.Message;
import com.example.demo.Room;
import com.example.demo.Users;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.PrivateMessageDTO;
import com.example.demo.dto.RoomNotificationDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.HiddenMessageRepository;
import com.example.demo.repositories.RoomRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.kafka.KafkaProducerService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final KafkaProducerService kafkaProducerService;
    private final HiddenMessageRepository hiddenMessageRepository;

    @Autowired
    public MessageController(MessageRepository messageRepository,
                             MessageService messageService,
                             SimpMessagingTemplate messagingTemplate,
                             UserRepository userRepository,
                             RoomRepository roomRepository,
                             KafkaProducerService kafkaProducerService,
                             HiddenMessageRepository hiddenMessageRepository) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.hiddenMessageRepository = hiddenMessageRepository;
    }

    // --- УДАЛЕНИЕ ---
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id,
                                                @RequestParam(defaultValue = "false") boolean forEveryone,
                                                Principal principal) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        Long roomId = message.getRoom().getId();

        Users currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
        boolean isOwner = message.getUsers() != null
                && message.getUsers().getUsername().equals(currentUser.getUsername());

        if (forEveryone) {
            if (!isOwner) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Чужое сообщение нельзя удалить для всех"
                );
            }
            hiddenMessageRepository.deleteByMessage(message);
            messageRepository.delete(message);
        } else {
            hiddenMessageRepository.findByUserAndMessage(currentUser, message)
                    .orElseGet(() -> hiddenMessageRepository.save(new HiddenMessage(currentUser, message)));
        }

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
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (message.getUsers() == null || !message.getUsers().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Редактирование чужих сообщений запрещено"
            );
        }
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
    public List<MessageDTO> getMessages(@PathVariable Long roomId, Principal principal) {
        List<Message> messages = messageRepository.findByRoomId(roomId);
        if (principal != null) {
            Users currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
            java.util.Set<Long> hiddenMessageIds = hiddenMessageRepository.findByUser(currentUser).stream()
                    .map(hiddenMessage -> hiddenMessage.getMessage().getId())
                    .collect(java.util.stream.Collectors.toSet());
            messages = messages.stream()
                    .filter(message -> !hiddenMessageIds.contains(message.getId()))
                    .toList();
        }
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @PostMapping("/write")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO, Principal principal) {
        if (messageDTO.getRoomId() == null) {
            return ResponseEntity.badRequest().body("Room ID is required");
        }
        boolean isMainRoom = Long.valueOf(1L).equals(messageDTO.getRoomId());

        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setAttachmentUrl(messageDTO.getAttachmentUrl());

        // ✅ ИСПРАВЛЕНИЕ: Используем LocalDateTime вместо Date
        message.setTimestamp(LocalDateTime.now());

        Room room = roomRepository.findById(messageDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found: " + messageDTO.getRoomId()));
        message.setRoom(room);

        Users user = null;
        String username = "Anonymous";

        if (principal == null) {
            if (!isMainRoom) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        } else {
            // ИСПРАВЛЕНИЕ ЛЯМБДЫ: создаем отдельную переменную, которая не меняется (effectively final)
            String currentUsername = principal.getName();
            username = currentUsername; // Обновляем внешнюю переменную для логики ниже (Kafka/Notifications)

            user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
            message.setUsers(user);
        }

        messageRepository.save(message);

        try {
            String kafkaPayload = String.format(
                    "roomId=%d; user=%s; content=%s",
                    message.getRoom().getId(),
                    username,
                    message.getContent()
            );

            kafkaProducerService.sendMessage("chat-messages", kafkaPayload);
        } catch (Exception e) {
            // ВАЖНО: не ломаем чат, если Kafka недоступна
            System.err.println("Kafka send failed: " + e.getMessage());
        }

        MessageDTO responseMessage = convertToDTO(message);

        messagingTemplate.convertAndSend("/topic/messages/" + message.getRoom().getId(), responseMessage);

        sendRoomNotifications(room, message, user, username);

        return ResponseEntity.ok("Сообщение успешно сохранено и отправлено через WebSocket");
    }

    // Вспомогательный метод
    private MessageDTO convertToDTO(Message message) {
        UserDTO userDTO = message.getUsers() == null
                ? new UserDTO(null, "Anonymous", null)
                : new UserDTO(
                message.getUsers().getId(),
                message.getUsers().getUsername(),
                message.getUsers().getAvatarUrl()
        );
        return new MessageDTO(
                message.getId(),
                message.getRoom().getId(),
                message.getContent(),
                message.getAttachmentUrl(),
                message.getTimestamp().atOffset(ZoneOffset.UTC).toString(),
                message.getUsers() == null ? null : message.getUsers().getId(),
                userDTO
        );
    }

    private void sendRoomNotifications(Room room, Message message, Users sender, String senderUsername) {
        if (room.getUsers() == null || room.getUsers().isEmpty()) {
            return;
        }

        RoomNotificationDTO notification = new RoomNotificationDTO(
                room.getId(),
                message.getId(),
                message.getContent(),
                message.getTimestamp().atOffset(ZoneOffset.UTC).toString(),
                sender == null ? null : sender.getId(),
                senderUsername
        );

        room.getUsers().stream()
                .filter(user -> sender == null || !user.getId().equals(sender.getId()))
                .forEach(user -> messagingTemplate.convertAndSendToUser(
                        user.getUsername(),
                        "/queue/room-notifications",
                        notification
                ));
    }
}