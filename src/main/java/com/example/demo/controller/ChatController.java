package com.example.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/sendMessage") // Приём сообщений от клиента
    @SendTo("/topic/messages") // Рассылка сообщений подписчикам
    public String handleChatMessage(String message) {
        return message; // Возвращаем сообщение для подписчиков
    }
}

