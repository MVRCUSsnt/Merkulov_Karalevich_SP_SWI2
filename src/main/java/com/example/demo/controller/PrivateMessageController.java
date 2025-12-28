package com.example.demo.controller;

import com.example.demo.PrivateMessage;
import com.example.demo.Users;
import com.example.demo.dto.PrivateMessageDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PrivateMessageRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/private-messages")
public class PrivateMessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final PrivateMessageRepository privateMessageRepository;

    public PrivateMessageController(MessageService messageService, UserRepository userRepository, PrivateMessageRepository privateMessageRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.privateMessageRepository = privateMessageRepository;
    }

    // 1. Отправка сообщений
    @PostMapping("/send")
    public ResponseEntity<PrivateMessageDTO> sendPrivateMessage(@RequestBody @Valid PrivateMessageDTO messageDTO,
                                                                Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        // Используем сервис для отправки, так как там есть логика уведомлений (WebSocket/Kafka)
        PrivateMessageDTO response = messageService.sendPrivateMessage(messageDTO, principal.getName());
        return ResponseEntity.ok(response);
    }



    // 2. Получение истории переписки (ИСПРАВЛЕНО)
    // Принимает username (String), потому что фронтенд присылает имя (например, "Katka")
    @GetMapping("/{recipientUsername}")
    public ResponseEntity<List<PrivateMessageDTO>> getChatHistory(@PathVariable String recipientUsername, Principal principal) {
        String currentUsername = principal.getName();

        // 1. Находим Себя (Отправителя)
        Users sender = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + currentUsername));

        // 2. Находим Собеседника (Получателя) по ИМЕНИ
        Users recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found: " + recipientUsername));

        // 3. Достаем переписку с помощью исправленного метода репозитория
        List<PrivateMessage> messages = privateMessageRepository.findChatHistory(sender, recipient);

        // 4. Превращаем сущности в DTO для отправки на фронт
        List<PrivateMessageDTO> dtos = messages.stream()
                .map(message -> new PrivateMessageDTO(
                        message.getRecipient().getId(),
                        message.getSender().getUsername(),
                        message.getRecipient().getUsername(),
                        message.getContent(),
                        message.getTimestamp().toString() // ISO формат времени
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 3. Удаление сообщений
    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> deletePrivateMessage(@PathVariable Long messageId, Principal principal) {
        messageService.deletePrivateMessage(messageId, principal.getName());
        return ResponseEntity.ok("Message deleted successfully");
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<UserDTO>> getConversations(Principal principal) {
        String currentUsername = principal.getName();

        // 1. Кто писал мне
        List<Users> senders = privateMessageRepository.findSendersByRecipient(currentUsername);
        // 2. Кому писал я
        List<Users> recipients = privateMessageRepository.findRecipientsBySender(currentUsername);

        // 3. Объединяем в один список (Set уберет дубликаты, если мы общались взаимно)
        Set<Users> conversationPartners = new HashSet<>();
        conversationPartners.addAll(senders);
        conversationPartners.addAll(recipients);

        // 4. Превращаем в DTO
        List<UserDTO> partnerDTOs = conversationPartners.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getAvatarUrl()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(partnerDTOs);
    }
}
