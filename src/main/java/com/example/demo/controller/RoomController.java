package com.example.demo.controller;

import com.example.demo.Room;
import com.example.demo.Users;
import com.example.demo.dto.RoomDTO;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
@RestController

public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }


    @PostMapping("/create")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO, Principal principal) {
        Room room = roomService.createRoom(roomDTO, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(roomDTO);
    }


    @PostMapping("/rooms/{roomId}/addUser")
    public ResponseEntity<String> addUserToRoom(@PathVariable Long roomId, @RequestBody Long userId, Principal principal) {
        roomService.addUserToRoom(roomId, userId, principal.getName());
        return ResponseEntity.ok("User added successfully");
    }
}
