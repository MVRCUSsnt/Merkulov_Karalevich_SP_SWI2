package com.example.demo.security;

import com.example.demo.utils.JwtUtil;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    @NonNull
    public Message<?> preSend(@NonNull Message<?> message,
                              @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        StompCommand command = accessor.getCommand();

        // Проверка на null для команды и её соответствие CONNECT
        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            // Проверка на null для authHeader и его соответствие "Bearer "
            if (!authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.validateToken(token); // Проверяем токен
                accessor.setUser(() -> username); // Устанавливаем имя пользователя
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid JWT token", e);
            }
        }

        return message;
    }
}
