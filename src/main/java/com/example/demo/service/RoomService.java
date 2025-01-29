package com.example.demo.service;

import com.example.demo.Room;
import com.example.demo.repositories.RoomRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.Users;
import com.example.demo.dto.RoomDTO;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public void createRoom(RoomDTO roomDTO, String creatorUsername) {
        Users creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + creatorUsername));

        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setDescription(roomDTO.getDescription());
        room.setCreator(creator);
        room.getUsers().add(creator);
        roomRepository.save(room);
    }



    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    public List<Room> getUserRooms(String username, int page, int size) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        return roomRepository.findByUsersContaining(user, pageable).getContent();
    }


    public Room updateRoom(Long id, RoomDTO roomDTO) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setName(roomDTO.getName());
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }


    public void addUserToRoom(Long roomId, Long userId, String creatorUsername) {

        // Проверяем, существует ли комната
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        // Проверяем, существует ли пользователь
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Проверяем, является ли текущий пользователь администратором комнаты
        if (!room.getCreator().getUsername().equals(creatorUsername)) {
            throw new IllegalArgumentException("Only the creator can add users to the room");
        }

        // Добавляем пользователя в комнату
        room.getUsers().add(user);
        roomRepository.save(room); // Сохраняем изменения
    }
}
