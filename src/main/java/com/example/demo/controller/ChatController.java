package com.example.demo.controller;

import com.example.demo.Room;
import com.example.demo.dto.RoomDTO;
import com.example.demo.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final RoomService roomService;

    public ChatController(RoomService roomService) {
        this.roomService = roomService;
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public String handleChatMessage(String message) {
        return message;
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createChat(@RequestBody @Valid RoomDTO roomDTO, Principal principal) {
        roomService.createRoom(roomDTO, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(roomDTO);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Room> getChatById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Room> updateChat(@PathVariable Long id, @RequestBody @Valid RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}


