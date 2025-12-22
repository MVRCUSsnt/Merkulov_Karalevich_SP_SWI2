package com.example.demo.controller;

import com.example.demo.Room;
import com.example.demo.Users;
import com.example.demo.dto.RoomDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")

public class RoomController {

    private final RoomService roomService;
    private  final  UserService userService;

    public RoomController(RoomService roomService, UserService userService) {

        this.roomService = roomService;
        this.userService = userService;
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

    @GetMapping("/{roomID}/users")
    public ResponseEntity<List<UserDTO>> getUsersInRoom(@PathVariable Long roomID, Principal principal) {
        List<UserDTO> users = roomService.getUsersInRoom(roomID, principal.getName());
        return ResponseEntity.ok(users);
    }


    @GetMapping("/addUser/{roomId}/{userName}")
    public ResponseEntity<String> addUserToRoom(@PathVariable Long roomId, @PathVariable String userName, Principal principal) {
        Long userId = userService.getUserByUsername(userName).getId();
        roomService.addUserToRoom(roomId, userId, principal.getName());
        return ResponseEntity.ok("User added successfully");
    }
}
