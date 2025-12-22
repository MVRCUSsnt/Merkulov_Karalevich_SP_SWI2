package com.example.demo.service;

import com.example.demo.Room;
import com.example.demo.dto.UserDTO;
import com.example.demo.repositories.RoomRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.Users;
import com.example.demo.dto.RoomDTO;
import com.example.demo.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public void createRoom(RoomDTO roomDTO, String creatorUsername) {
        Users creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + creatorUsername));

        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setDescription(roomDTO.getDescription());
        room.setCreator(creator);

        // Добавляем пользователя в комнату
        room.getUsers().add(creator);
        creator.getRooms().add(room);

        // Сохраняем данные
        roomRepository.save(room);
        userRepository.save(creator);
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

    public List<UserDTO> getUsersInRoom(Long roomID, String username) {
        // Получаем текущего пользователя
        Users currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Получаем комнату по ID
        Room room = roomRepository.findById(roomID)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Проверяем, состоит ли текущий пользователь в этой комнате
        if (!room.getUsers().contains(currentUser)) {
            throw new AccessDeniedException("You are not a member of this room");
        }

        // Преобразуем список пользователей комнаты в DTO
        return room.getUsers().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getAvatarUrl()))
                .collect(Collectors.toList());
    }

    @Transactional
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
        user.getRooms().add(room);

        roomRepository.save(room);
        userRepository.save(user);
    }


}
