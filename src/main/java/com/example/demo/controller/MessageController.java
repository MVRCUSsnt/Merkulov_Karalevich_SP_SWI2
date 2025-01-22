package com.example.demo.controller;

import com.example.demo.Message;
import com.example.demo.MessageRepository;
import com.example.demo.Room;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/{room}")
    public List<Message> getMessages(@PathVariable Room room) {
        return messageRepository.findByRoom(room);
    }


    @PostMapping
    public Message sendMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }
}
