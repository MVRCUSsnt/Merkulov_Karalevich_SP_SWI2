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

import java.security.Principal;
import java.util.List;
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

    @PostMapping
    public List<PrivateMessageDTO> getPrivateMessages(String senderUsername, String recipientUsername) {
        Users sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        Users recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        List<PrivateMessage> messages = privateMessageRepository
                .findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(sender, recipient, recipient, sender);

        return messages.stream()
                .map(message -> new PrivateMessageDTO(
                        message.getRecipient().getId(),
                        message.getSender().getUsername(),
                        message.getRecipient().getUsername(),
                        message.getContent(),
                        message.getTimestamp().toString()
                ))
                .collect(Collectors.toList());
    }



    @GetMapping("/{senderId}")
    public ResponseEntity<List<PrivateMessageDTO>> getMessages(@PathVariable Long senderId, Principal principal) {
        Users sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        UserDTO senderDTO = new UserDTO(sender.getId(), sender.getUsername(), sender.getEmail());

        List<PrivateMessageDTO> messages = messageService.getPrivateMessages(senderDTO.getUsername(), principal.getName());

        return ResponseEntity.ok(messages);
    }


    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> deletePrivateMessage(@PathVariable Long messageId, Principal principal) {
        messageService.deletePrivateMessage(messageId, principal.getName());
        return ResponseEntity.ok("Message deleted successfully");
    }


}
