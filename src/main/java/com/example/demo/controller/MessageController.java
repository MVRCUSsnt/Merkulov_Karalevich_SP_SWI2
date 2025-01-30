package com.example.demo.controller;

import com.example.demo.Message;
import com.example.demo.Room;
import com.example.demo.Users;
import com.example.demo.dto.PrivateMessageDTO;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.Users;
import com.example.demo.Users;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final UserRepository userRepository;

    @Autowired
    public MessageController(MessageRepository messageRepository, MessageService messageService, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id, Principal principal) {
        messageService.deleteMessage(id, principal);
        return ResponseEntity.ok("Message deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<PrivateMessageDTO>> getFilteredMessages(
            @RequestParam(required = false) String filter
    ) {
        List<PrivateMessageDTO> messages = messageService.getFilteredMessages(filter);
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/{roomId}")
    public List<MessageDTO> getMessages(@PathVariable Long roomId) {
        List<Message> messages = messageRepository.findByRoomId(roomId);
        return messages.stream()
                .map(message -> new MessageDTO(
                        message.getId(),
                        message.getRoom().getId(),
                        message.getContent(),
                        message.getTimestamp().toString(),
                        new UserDTO(
                                message.getUsers().getId(),
                                message.getUsers().getUsername(),
                                message.getUsers().getAvatarUrl()
                        )
                ))
                .collect(Collectors.toList());
    }


    @PostMapping
    public Message sendMessage(@RequestBody Message message, Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("User must be authenticated");
        }
        Users user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        message.setUsers(user);
        return messageRepository.save(message);
    }

    @PostMapping("/write")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        Message message = new Message();
        message.setContent(messageDTO.getContent());
        // Установите room и users в зависимости от вашей логики
        Room room = new Room(); // Найдите сущность Room на основе roomId из messageDTO
        room.setId(messageDTO.getRoomId());
        message.setRoom(room);

        Users user = new Users(); // Найдите сущность Users на основе userId из messageDTO
        user.setId(messageDTO.getUserId());
        message.setUsers(user);

        messageRepository.save(message);
        return ResponseEntity.ok("Сообщение успешно сохранено");
    }


}
