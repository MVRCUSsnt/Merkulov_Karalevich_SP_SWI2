package com.example.demo.controller;

import com.example.demo.Message;
import com.example.demo.MessageRepository;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.UserDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

//    @GetMapping("/{roomId}")
//    public List<Message> getMessages(@PathVariable Long roomId) {
//        return messageRepository.findByRoomId(roomId);
//    }

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
    public Message sendMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }


}
