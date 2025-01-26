package com.example.demo.controller;

import com.example.demo.Message;
import com.example.demo.MessageRepository;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.MessageService;
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

    public MessageController(MessageRepository messageRepository, MessageService messageService) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id, Principal principal) {
        messageService.deleteMessage(id, principal);
        return ResponseEntity.ok("Message deleted successfully");
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



    @PostMapping("/write")
    public Message sendMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }


}
