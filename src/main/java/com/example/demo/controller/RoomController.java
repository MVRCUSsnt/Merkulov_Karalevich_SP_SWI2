package com.example.demo.controller;

import com.example.demo.Room;
import com.example.demo.dto.RoomDTO;
import com.example.demo.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController

public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<List<Room>> getUserRooms(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Room> rooms = roomService.getUserRooms(principal.getName(), page, size);
        return ResponseEntity.ok(rooms);
    }


    @PostMapping("/create")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO, Principal principal) {
        roomService.createRoom(roomDTO, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(roomDTO);
    }


    @PostMapping("/rooms/{roomId}/addUser")
    public ResponseEntity<String> addUserToRoom(@PathVariable Long roomId, @RequestBody Long userId, Principal principal) {
        roomService.addUserToRoom(roomId, userId, principal.getName());
        return ResponseEntity.ok("User added successfully");
    }
}
