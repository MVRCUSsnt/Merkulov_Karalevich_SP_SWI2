package com.example.demo.controller;

import com.example.demo.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @GetMapping("/{room}")
    public List<Message> getMessages(@PathVariable String room) { /* ... */
        return null;
    }

    @PostMapping
    public Message sendMessage(@RequestBody Message message) { /* ... */
        return message;
    }
}
