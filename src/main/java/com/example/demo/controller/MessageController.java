package com.example.demo.controller;

import com.example.demo.Message;
import com.example.demo.Room;
import com.example.demo.Users;
import com.example.demo.dto.PrivateMessageDTO;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.stream.Collectors;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private  final UserService userService;

    private final UserRepository userRepository;

    @Autowired
    public MessageController(MessageRepository messageRepository, MessageService messageService, SimpMessagingTemplate messagingTemplate, UserService userService, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id, Principal principal) {
        messageService.deleteMessage(id, principal);
        return ResponseEntity.ok("Message deleted successfully");
    }
    @PutMapping("/edit/{id}")
    public ResponseEntity<Message> editMessage(@PathVariable Long id, @RequestBody MessageDTO messageDTO, Principal principal) {
        Message updatedMessage = messageService.updateMessage(id, messageDTO);
        return ResponseEntity.ok(updatedMessage);
    }


    @GetMapping
    public ResponseEntity<List<PrivateMessageDTO>> getFilteredMessages(
            @RequestParam(required = false) String filter
    ) {
        List<PrivateMessageDTO> messages = messageService.getFilteredMessages(filter);
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/{roomId}")
    public List<MessageDTO> getMessages(@PathVariable Long roomId) {
        List<Message> messages = messageRepository.findByRoomId(roomId);
        return messages.stream()
                .map(message -> new MessageDTO(
                        message.getId(),
                        message.getRoom().getId(),
                        message.getContent(),
                        message.getTimestamp().toString(),
                        new UserDTO(
                                message.getUsers().getId(),
                                message.getUsers().getUsername(),
                                message.getUsers().getAvatarUrl()
                        )
                ))
                .collect(Collectors.toList());
    }



    @PostMapping("/write")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        Message message = new Message();
        message.setContent(messageDTO.getContent());

        Room room = new Room();
        room.setId(messageDTO.getRoomId());
        message.setRoom(room);

        Users user = new Users();
        user = userService.getUserById((messageDTO.getUserDTO().getId()));
        message.setUsers(user);

        messageRepository.save(message);

        // 🔹 Создаём DTO-ответ с сохранённым ID и данными пользователя
        MessageDTO responseMessage = new MessageDTO(
                message.getId(),
                message.getRoom().getId(),
                message.getContent(),
                message.getTimestamp().toString(),
                new UserDTO(
                        message.getUsers().getId(),
                        message.getUsers().getUsername(),
                        message.getUsers().getAvatarUrl()
                )
        );

        // 🔹 Отправляем сообщение в WebSocket
        messagingTemplate.convertAndSend("/topic/messages/" + message.getRoom().getId(), responseMessage);

        return ResponseEntity.ok("Сообщение успешно сохранено и отправлено через WebSocket");
    }




}
